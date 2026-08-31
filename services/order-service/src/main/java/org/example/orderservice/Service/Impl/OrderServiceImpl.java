package org.example.orderservice.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.Message.OrderDelayMessage;
import org.example.common.dto.OrdinaryOrderDTO;
import org.example.common.dto.UserDto;
import org.example.common.utils.UserHolder;
import org.example.common.vo.OrdinaryOrderVO;
import org.example.orderservice.Service.OrderService;
import org.example.orderservice.entity.Order;
import org.example.orderservice.mapper.GoodsMapper;
import org.example.orderservice.mapper.OrderMapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final GoodsMapper goodsMapper;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public OrdinaryOrderVO createOrder(OrdinaryOrderDTO dto) {
        UserDto user = UserHolder.getUser();
        Long userId = user.getId();

        // 1. 分布式锁：防止同一用户对同一商品重复下单
        String lockKey = ORDER_LOCK_KEY + userId + ":" + dto.getGoodsId();
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLock = false;
        try {
            isLock = lock.tryLock(0, 10, TimeUnit.SECONDS);
            if (!isLock) {
                throw new RuntimeException("请勿重复提交订单");
            }

            // 2. 查商品状态
            Map<String, Object> goods = goodsMapper.selectGoodsById(dto.getGoodsId());
            if (goods == null) {
                throw new RuntimeException("商品不存在");
            }
            Integer status = (Integer) goods.get("status");
            if (status == null || status != 1) {
                throw new RuntimeException("商品已下架或不可购买");
            }

            // 3. Redis INCR 生成订单号
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            Long seq = stringRedisTemplate.opsForValue().increment(ORDER_SEQ_KEY + today);
            String orderNo = today + String.format("%06d", seq);

            // 4. 组装订单
            BigDecimal price = (BigDecimal) goods.get("selling_price");
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setBuyerId(userId.intValue());
            order.setSellerId(((Number) goods.get("seller_id")).intValue());
            order.setGoodsId(dto.getGoodsId().intValue());
            order.setGoodsTitle((String) goods.get("title"));
            order.setGoodsImage((String) goods.get("cover_image"));
            order.setGoodsPrice(price);
            order.setQuantity(1);
            order.setTotalPrice(price);
            order.setIsSeckill(0);
            order.setStatus(0); // 待支付
            order.setPayStatus(0);
            order.setBuyerWay(dto.getTradeMethod() != null ? dto.getTradeMethod() : 1);
            order.setAddress(dto.getAddress());
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());

            save(order);

            // 6. 商品状态改为已锁定
            goodsMapper.updateStatus(dto.getGoodsId(), 2);

            // 7. Redis Hash 缓存订单（TTL 30分钟）
            Map<String, String> cacheMap = new HashMap<>();
            cacheMap.put("orderNo", orderNo);
            cacheMap.put("status", "0");
            cacheMap.put("amount", price.toPlainString());
            cacheMap.put("userId", userId.toString());
            cacheMap.put("goodsId", dto.getGoodsId().toString());
            String cacheKey = ORDER_CACHE_KEY + orderNo;
            stringRedisTemplate.opsForHash().putAll(cacheKey, cacheMap);
            stringRedisTemplate.expire(cacheKey, ORDER_CACHE_TTL, TimeUnit.MINUTES);
            OrderDelayMessage msg = new OrderDelayMessage();
            msg.setOrderId(order.getId());
            msg.setGoodsId(dto.getGoodsId());
            msg.setDelaysMillList(Arrays.asList(10_000L, 30_000L, 60_000L, 120_000L));
            // 8. 先取出第一个延迟时间，再发消息（序列化后列表少一个元素，消费者收到后可继续延迟）
            Long firstDelay = msg.getNextDelay();
            rabbitTemplate.convertAndSend(
                    "order-delay-exchange",
                    "order.delay",
                    msg,
                    message -> {
                        message.getMessageProperties().setHeader("x-delay", firstDelay);
                        return message;
                    }
            );
            log.info("订单创建成功，订单号: {}，已发送延迟关单消息", orderNo);

            // 9. 返回 VO
            long expireTime = System.currentTimeMillis() + ORDER_CACHE_TTL * 60 * 1000;
            OrdinaryOrderVO vo = new OrdinaryOrderVO();
            vo.setOrderNo(orderNo);
            vo.setStatus(0);
            vo.setAmount(price);
            vo.setExpireTime(expireTime);
            return vo;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("系统繁忙，请重试");
        } finally {
            if (isLock && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
