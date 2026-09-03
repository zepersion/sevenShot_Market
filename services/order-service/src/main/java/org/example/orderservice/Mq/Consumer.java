package org.example.orderservice.Mq;

import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.common.Message.OrderCompleteMessage;
import org.example.common.Message.OrderDelayMessage;
import org.example.common.Message.SeckillOrderMessage;
import org.example.orderservice.FeignClient.UserFeignClient;
import org.example.orderservice.entity.Order;
import org.example.orderservice.mapper.GoodsMapper;
import org.example.orderservice.mapper.OrderMapper;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.example.common.RedisConstants.*;

@Slf4j
@Component
public class Consumer {

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private UserFeignClient userFeignClient;

    // ==================== 秒杀异步创建订单 ====================
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "seckill-order-queue"),
            exchange = @Exchange(name = "seckill-order-exchange", type = ExchangeTypes.DIRECT),
            key = "seckill.order"
    ))
    public void createSeckillOrder(SeckillOrderMessage msg, Channel channel, long deliveryTag) throws IOException {
        log.info("收到秒杀订单消息: userId={}, goodsId={}", msg.getUserId(), msg.getGoodsId());
        try {
            Map<String, Object> goods = goodsMapper.selectGoodsById(msg.getGoodsId());
            if (goods == null) {
                log.error("秒杀商品不存在: goodsId={}", msg.getGoodsId());
                channel.basicAck(deliveryTag, false);
                return;
            }
            Integer status = (Integer) goods.get("status");
            if (status == null || status != 4) {
                log.error("秒杀商品状态异常: goodsId={}, status={}", msg.getGoodsId(), status);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 生成订单号
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            Long seq = stringRedisTemplate.opsForValue().increment(ORDER_SEQ_KEY + today);
            String orderNo = today + String.format("%06d", seq);

            // 组装订单
            BigDecimal price = (BigDecimal) goods.get("selling_price");
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setBuyerId(msg.getUserId());
            order.setSellerId((Long) goods.get("seller_id"));
            order.setGoodsId( msg.getGoodsId());
            order.setGoodsTitle((String) goods.get("title"));
            order.setGoodsImage((String) goods.get("cover_image"));
            order.setGoodsPrice(price);
            order.setQuantity(1);
            order.setTotalPrice(price);
            order.setIsSeckill(1);
            order.setStatus(0);
            order.setPayStatus(0);
            order.setBuyerWay(msg.getTradeWay() != null ? msg.getTradeWay() : 1);
            order.setAddress(msg.getAddress());
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.insert(order);

            // 更新商品状态和库存
            goodsMapper.updateStatus(msg.getGoodsId(), 2);
            goodsMapper.updateSeckillStock1(msg.getGoodsId());

            // Redis 缓存订单
            Map<String, String> cacheMap = new HashMap<>();
            cacheMap.put("orderNo", orderNo);
            cacheMap.put("status", "0");
            cacheMap.put("amount", price.toPlainString());
            cacheMap.put("userId", msg.getUserId().toString());
            cacheMap.put("goodsId", msg.getGoodsId().toString());
            String cacheKey = ORDER_CACHE_KEY + orderNo;
            stringRedisTemplate.opsForHash().putAll(cacheKey, cacheMap);
            stringRedisTemplate.expire(cacheKey, ORDER_CACHE_TTL, TimeUnit.MINUTES);


            OrderDelayMessage delayMsg = new OrderDelayMessage();
            delayMsg.setOrderId(order.getId());
            delayMsg.setGoodsId(msg.getGoodsId());
            delayMsg.setDelaysMillList(Arrays.asList(10_000L, 30_000L, 60_000L, 120_000L));
            Long firstDelay = delayMsg.getNextDelay();
            sendDelayMessage(delayMsg, firstDelay, "seckill-delay-exchange", "seckill.delay");

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("秒杀订单创建异常", e);
            channel.basicNack(deliveryTag, false, true);
        }
    }


    @RabbitListener(queues = "seckill-order-delay-queue")
    public void listenSeckillDelayOrder(OrderDelayMessage message, Channel channel, long deliveryTag) throws IOException {
        try {
            Long orderId = message.getOrderId();

            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                log.error("订单不存在: orderId={}", orderId);
                channel.basicAck(deliveryTag, false);
                return;
            }
            if (order.getStatus() != 0) {
                log.info("秒杀订单{}已支付，结束延迟链", orderId);
                channel.basicAck(deliveryTag, false);
                return;
            }

            if (message.hasMoreDelay()) {
                Long nextDelay = message.getNextDelay();
                log.info("秒杀订单{}未支付，重发延迟消息，下次延迟{}ms", orderId, nextDelay);
                sendDelayMessage(message, nextDelay, "seckill-delay-exchange", "seckill.delay");
            } else {
                log.info("秒杀订单{}超时未支付，执行关单+恢复库存", orderId);
                // 关单
                Order updateOrder = new Order();
                updateOrder.setId(orderId);
                updateOrder.setStatus(5);
                updateOrder.setUpdateTime(LocalDateTime.now());
                orderMapper.updateById(updateOrder);
                // 恢复秒杀库存
                goodsMapper.updateSeckillStock(message.getGoodsId());
                // 商品状态恢复为秒杀中(4)
                goodsMapper.updateStatus(message.getGoodsId(), 4);
                // 清除订单缓存
                stringRedisTemplate.delete(ORDER_CACHE_KEY + order.getOrderNo());
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("秒杀延迟关单消费异常", e);
            channel.basicNack(deliveryTag, false, true);
        }
    }


    @RabbitListener(queues = "order-delay-queue")
    public void listenOrdinaryDelayOrder(OrderDelayMessage message, Channel channel, long deliveryTag) throws IOException {
        try {
            Long orderId = message.getOrderId();

            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                log.error("订单不存在: orderId={}", orderId);
                channel.basicAck(deliveryTag, false);
                return;
            }
            if (order.getStatus() != 0) {
                log.info("普通订单{}已支付，结束延迟链", orderId);
                channel.basicAck(deliveryTag, false);
                return;
            }

            if (message.hasMoreDelay()) {
                Long nextDelay = message.getNextDelay();
                log.info("普通订单{}未支付，重发延迟消息，下次延迟{}ms", orderId, nextDelay);
                sendDelayMessage(message, nextDelay, "order-delay-exchange", "order.delay");
            } else {
                log.info("普通订单{}超时未支付，执行关单+恢复商品状态", orderId);
                // 关单
                Order updateOrder = new Order();
                updateOrder.setId(orderId);
                updateOrder.setStatus(5);
                updateOrder.setUpdateTime(LocalDateTime.now());
                orderMapper.updateById(updateOrder);
                // 普通订单：商品状态回退为可售(1)
                goodsMapper.updateStatus(message.getGoodsId(), 1);
                // 清除订单缓存
                stringRedisTemplate.delete(ORDER_CACHE_KEY + order.getOrderNo());
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("普通延迟关单消费异常", e);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    // ==================== 发送延迟消息（统一用对象，Jackson 自动序列化） ====================
    public void sendDelayMessage(OrderDelayMessage msg, Long delay, String exchange, String routingKey) {
        rabbitTemplate.convertAndSend(exchange, routingKey, msg, message -> {
            message.getMessageProperties().setHeader("x-delay", delay);
            return message;
        });
    }

    // ==================== 订单完成异步处理 ====================

    // 1. 更新卖家信用分
    @RabbitListener(queues = "order.credit.queue")
    public void updateCredit(OrderCompleteMessage msg, Channel channel, long deliveryTag) throws IOException {
        try {
            userFeignClient.updateCredit(msg.getSellerId(), 1);
            log.info("卖家{}信用分+1, orderId={}", msg.getSellerId(), msg.getOrderId());
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("更新卖家信用分失败: sellerId={}", msg.getSellerId(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    // 2. 推送通知
    @RabbitListener(queues = "order.notify.queue")
    public void sendNotify(OrderCompleteMessage msg, Channel channel, long deliveryTag) throws IOException {
        try {
            log.info("发送通知: 买家{}确认收货, 订单完成, 卖家{}", msg.getBuyerId(), msg.getSellerId());
            // TODO: 实现站内信/推送通知
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("发送通知失败", e);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    // 3. 商品标记已售
    @RabbitListener(queues = "order.goods.sold.queue")
    public void markGoodsSold(OrderCompleteMessage msg, Channel channel, long deliveryTag) throws IOException {
        try {
            goodsMapper.updateStatus(msg.getGoodsId(), 6);
            stringRedisTemplate.delete(GOODS_INFO_KEY + msg.getGoodsId());
            log.info("商品{}已标记为已售, orderId={}", msg.getGoodsId(), msg.getOrderId());
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("商品标记已售失败: goodsId={}", msg.getGoodsId(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }
}
