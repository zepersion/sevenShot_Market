package org.example.orderservice.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.Message.OrderDelayMessage;
import org.example.common.Result;
import org.example.common.dto.OrdinaryOrderDTO;
import org.example.common.dto.UserDto;
import org.example.common.utils.UserHolder;
import org.example.common.vo.OrderDetailVO.BuyerVO;
import org.example.common.vo.OrderDetailVO.DetailOrderVO;
import org.example.common.vo.OrderDetailVO.OrderGoodsVO;
import org.example.common.vo.OrderDetailVO.SellerVO;
import org.example.common.vo.OrderListVO;
import org.example.common.vo.OrderResultVO;
import org.example.common.vo.OrdinaryOrderVO;
import org.example.common.vo.PageVO;
import org.example.orderservice.FeignClient.UserFeignClient;
import org.example.orderservice.Service.OrderService;
import org.example.orderservice.entity.Order;
import org.example.orderservice.entity.OrderStatusLog;
import org.example.orderservice.mapper.GoodsMapper;
import org.example.orderservice.mapper.OrderMapper;
import org.example.orderservice.mapper.OrderStatusLogMapper;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.example.common.RedisConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    @Resource
    private  GoodsMapper goodsMapper;
    @Resource
    private  RedissonClient redissonClient;
    @Resource
     private final StringRedisTemplate stringRedisTemplate;
    @Resource
    private final RabbitTemplate rabbitTemplate;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private UserFeignClient userFeignClient;
    @Resource
    private OrderStatusLogMapper orderStatusLogMapper;
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
            order.setBuyerId(userId);
            order.setSellerId((Long) goods.get("seller_id"));
            order.setGoodsId(dto.getGoodsId());
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

    @Override
    public OrderResultVO getOrderByOrderNo(Long orderNo) {
       String key= ORDER_CACHE_KEY + orderNo;
        Object status = stringRedisTemplate.opsForHash().get(key, "status");
        Object orderId = stringRedisTemplate.opsForHash().get(key, "orderId");

        OrderResultVO vo= new OrderResultVO();
        if(status == null||orderId==null) {
            Order order = lambdaQuery().eq(Order::getOrderNo, orderNo).one();
            if(order==null) {
                return null;
            }
            vo.setOrderNo(orderNo);
            vo.setStatus(order.getStatus());
            vo.setOrderId(order.getId());
            vo.setFailReason(null);
            stringRedisTemplate.opsForHash().delete(key);
            stringRedisTemplate.opsForHash().put(key, "status", order.getStatus().toString());
            stringRedisTemplate.opsForHash().put(key, "orderId", order.getId());

            return vo;
        }
        vo.setOrderNo(orderNo);
        vo.setFailReason(null);
        vo.setOrderId((Long) orderId);
        vo.setStatus((Integer) status);
        return vo;
    }

    @Override
    public DetailOrderVO getOrderByid(Long id) throws JsonProcessingException {
        String key= ORDER_INFO_KEY + id;
        String s = stringRedisTemplate.opsForValue().get(key);
        if(s != null && !s.isBlank()) {

            DetailOrderVO vo = objectMapper.readValue(s, DetailOrderVO.class);
            return vo;
        }

        Order order = orderMapper.selectById(id);
        if (order == null) {
            return null;
        }
        DetailOrderVO vo = new DetailOrderVO();
        vo.setOrderNo(order.getOrderNo());

        if (order.getStatus() == 0) {
            // 创建时间 + 30分钟 = 过期时间
            LocalDateTime expireTime = order.getCreateTime().plusMinutes(30);
            vo.setExpireTime(expireTime);
        }
        vo.setCreateTime(order.getCreateTime());
        vo.setTradeway(order.getBuyerWay());
        vo.setFinishTime(order.getFinishTime());
        vo.setIsSeckill(order.getIsSeckill());
        vo.setDeliverTime(order.getDeliverTime());
        vo.setPayTime(order.getPayTime());
        //goods vo
        String goods = stringRedisTemplate.opsForValue().get(ORDER_GOODS_INFO_KEY + order.getGoodsId());
        if((goods != null && !goods.isBlank())){
            OrderGoodsVO orderGoodsVO = objectMapper.readValue(goods, OrderGoodsVO.class);
            vo.setGoodsVO(orderGoodsVO);
        }else {

            Map<String, Object> goodsInfo = goodsMapper.selectGoodsAllById(order.getGoodsId());
            if (goodsInfo == null) {
                log.error("{}不存在,订单有误", order.getGoodsId());
                return null;
            }
            OrderGoodsVO orderGoodsVO = new OrderGoodsVO();
            orderGoodsVO.setGoodsId((Long) goodsInfo.get("id"));
            orderGoodsVO.setQuantity(order.getQuantity());
            orderGoodsVO.setPrice((BigDecimal) goodsInfo.get("original_price"));//
            orderGoodsVO.setTitle(goodsInfo.get("title").toString());
            orderGoodsVO.setImageUrl(goodsInfo.get("cover_image").toString());//
            vo.setGoodsVO(orderGoodsVO);
            stringRedisTemplate.opsForValue().set(ORDER_GOODS_INFO_KEY + order.getGoodsId(), objectMapper.writeValueAsString(orderGoodsVO), 30, TimeUnit.SECONDS);
            //查询
        }
        //buyeruser
        String buyerKey =ORDER_GOODS_BUYER_KEY + order.getBuyerId();
        String buyer = stringRedisTemplate.opsForValue().get(buyerKey);

        if((buyer != null && !buyer.isBlank())){
            BuyerVO buyerVO = objectMapper.readValue(buyer, BuyerVO.class);
            vo.setBuyerVO(buyerVO);
        }
        else {
            Result<UserDto> user = userFeignClient.getUserById(order.getBuyerId());
            UserDto buyer0 = user.getData();
            if(buyer0 ==null) {
                log.error("{}不存在,商品信息有误", order.getBuyerId());
                return null;
            }
            BuyerVO buy_vo = new BuyerVO();
            buy_vo.setBuyerName(buyer0.getNickname());
            buy_vo.setAvatar(buyer0.getAvatar());
            buy_vo.setPhone(buyer0.getPhone());
            buy_vo.setUserId(buyer0.getId());
            vo.setBuyerVO(buy_vo);
            stringRedisTemplate.opsForValue().set(buyerKey,objectMapper.writeValueAsString(buy_vo), 30, TimeUnit.SECONDS);
        }
        //buyeruser

        String sellerKey = ORDER_GOODS_SELLER_KEY + order.getSellerId();
        String seller = stringRedisTemplate.opsForValue().get(sellerKey);

        if(seller!=null&&!seller.isBlank()){

            SellerVO sellerVO1 = objectMapper.readValue(seller, SellerVO.class);
            vo.setSellerVO(sellerVO1);
        }
        else {
            Result<UserDto> user = userFeignClient.getUserById(order.getSellerId());
            UserDto seller0= user.getData();
            if(seller0 ==null) {
                log.error("{}不存在,商品信息有误", order.getSellerId());
                return null;
            }
           SellerVO sellerVO = new SellerVO();
            sellerVO.setBuyerName(seller0.getNickname());
            sellerVO.setAvatar(seller0.getAvatar());
            sellerVO.setPhone(seller0.getPhone());
            sellerVO.setUserId(seller0.getId());
            vo.setSellerVO(sellerVO);
            stringRedisTemplate.opsForValue().set(sellerKey,objectMapper.writeValueAsString(sellerVO), 30, TimeUnit.SECONDS);
        }
        vo.setReceiveTime(order.getReceiveTime());
        vo.setOrderId(order.getId());
        vo.setStatus(setStatusName(order.getStatus()));
        return vo;
    }

    @Override
    public PageVO<OrderListVO> myOrderList(Integer role, Integer status, Integer page, Integer size, Long id) {
        Page<Order> pages = new Page<Order>(page,size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(role ==1,Order::getBuyerId,id)
                .eq(role==2,Order::getSellerId,id)
                .eq(status!=null,Order::getStatus,status)
                .orderByDesc(Order::getCreateTime);
        Page<Order> orderPage = orderMapper.selectPage(pages, wrapper);

        List<OrderListVO> list = orderPage.getRecords().stream().map(order ->
                {
                    OrderListVO vo = new OrderListVO();
                    vo.setOrderId(order.getId());
                    vo.setStatus(order.getStatus());
                    vo.setCreateTime(order.getCreateTime());
                    vo.setOrderNo(order.getOrderNo());
                    vo.setIsSeckill(order.getIsSeckill());
                    vo.setGoodsImage(order.getGoodsImage());
                    vo.setTotalPrice(order.getTotalPrice());
                    vo.setQuantity(order.getQuantity());
                    vo.setGoodsTitle(order.getGoodsTitle());
                    vo.setStatusName(setStatusName(order.getStatus()));
                    if (order.getStatus() == 0) {
                        vo.setExpireTime(order.getCreateTime().plusMinutes(30));
                    }
                    return vo;
                }
        ).collect(Collectors.toList());
        PageVO<OrderListVO> pageVo = new PageVO<>();
            pageVo.setTotal(orderPage.getTotal());
            pageVo.setRecords(list);
            pageVo.setSize(orderPage.getSize());
            pageVo.setCurrent(orderPage.getCurrent());
            return pageVo;
    }

    @Override
    public void cancel(Long id, String reason) {
        Long userId = UserHolder.getUser().getId();
        //拿出订单
        Order order = query().eq("id", id).one();
        if(order==null){
            log.error("id为{}的订单不存在", id);
            return;
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("无权取消他人订单");
        }
        // 校验：只有待支付才能取消
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态不支持取消");
        }
        LambdaUpdateChainWrapper<Order> eq = lambdaUpdate()
                .eq(Order::getId, id)
                .eq(Order::getStatus, 0).
                set(Order::getStatus, 5)
                .set(Order::getCancelTime, LocalDateTime.now())
                .set(Order::getCancelReason, reason);
        int update = orderMapper.update(null, eq);
        if(update==0){
            throw new RuntimeException("订单已经变更或者失效");
        }
        if(order.getIsSeckill()==1){
            goodsMapper.updateSeckillStock(order.getGoodsId());
            goodsMapper.updateStatus(order.getGoodsId(),4);
        }
        if(order.getIsSeckill()==0){
            goodsMapper.updateStock(order.getGoodsId());
            goodsMapper.updateStatus(order.getGoodsId(),1);
        }
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(id);
        log.setBefore_status(0);
        log.setAfter_status(5);
        log.setOperator_type(1);      // 1=买家
        log.setOperator_id(userId.intValue());
        log.setRemark("买家取消：" + reason);
        log.setCreate_time(LocalDateTime.now());
        orderStatusLogMapper.insert(log);

        stringRedisTemplate.delete(ORDER_CACHE_KEY + order.getOrderNo());
        stringRedisTemplate.delete( ORDER_INFO_KEY+order.getId());
    }

    private String setStatusName(Integer status) {

        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "待发货";
            case 2 -> "已发货";
            case 3 -> "已收货";
            case 4 -> "已完成";
            case 5 -> "已取消";
            default -> "未知";
        };
    }
}
