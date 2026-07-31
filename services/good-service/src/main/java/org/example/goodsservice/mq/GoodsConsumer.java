package org.example.goodsservice.mq;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.example.common.Message.GoodsFavoriteMessage;
import org.example.common.Message.GoodsLikeMessage;
import org.example.goodsservice.entity.Goods;
import org.example.goodsservice.entity.GoodsFavorite;
import org.example.goodsservice.entity.GoodsLike;
import org.example.goodsservice.mapper.GoodsFavoriteMapper;
import org.example.goodsservice.mapper.GoodsLikeMapper;
import org.example.goodsservice.mapper.GoodsMapper;
import org.example.common.Message.GoodsAuditTaskMessage;
import org.example.common.Message.GoodsPublishMQMessage;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static org.example.common.RedisConstants.GOODS_INFO_KEY;
import static org.example.common.RedisConstants.GOODS_LIKE_KEY;

@Component
@RequiredArgsConstructor
public class GoodsConsumer {
    @Resource
    private GoodsFavoriteMapper goodsFavoriteMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private GoodsLikeMapper goodsLikeMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "goods.audit.queue",durable = "true"),
            exchange =@Exchange(name = "goods.direct",type = ExchangeTypes.DIRECT),
            key = {"goods.audit"}
    ))
    public void consumerGoodsPublish(GoodsPublishMQMessage msg) {
        //组装数据
        Goods goods = new Goods();
        goods.setDegree(msg.getDegree());
        goods.setDescription(msg.getDescription());
        goods.setCategoryId(msg.getCategoryId());
        goods.setIsDonation(msg.getIsDonation());

        goods.setTitle(msg.getTitle());
        goods.setImages(JSONUtil.toJsonPrettyStr(msg.getImages()));
        goods.setStock(msg.getStock());
        goods.setOriginalPrice(msg.getOriginalPrice());
        goods.setTags(JSONUtil.toJsonPrettyStr(msg.getTags()));
        goods.setSellingPrice(msg.getSellingPrice());
        goods.setCoverImage(msg.getCoverImage());
        goods.setStatus(0); // 0代表待审核
        //
        goodsMapper.insert(goods);
        Long goodsId = goods.getId();
        GoodsAuditTaskMessage auditMsg = new GoodsAuditTaskMessage();
        auditMsg.setGoodsId(goodsId);
        auditMsg.setTitle(msg.getTitle());
        auditMsg.setDescription(msg.getDescription());
        auditMsg.setTags(msg.getTags());
        rabbitTemplate.convertAndSend("ai.audit.exchange", "ai.audit.queue", auditMsg);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "goods.like.queue",durable = "true"),
            exchange =@Exchange(name = "goods.like.exchange",type = ExchangeTypes.DIRECT),
            key = {"goodsLike"}
    ))
    public void GoodsLikePublish(GoodsLikeMessage msg) {
        //获取商品id
        Long goodsId = msg.getGoodsId();
        //获取用户id
        Long userId = msg.getUserId();
        //key
        String key=GOODS_LIKE_KEY+goodsId;
        //获取type
        Integer type = msg.getType();
        //创建我们的实体类
        GoodsLike goodsLike = new GoodsLike();
        if(type==1){
            goodsLike.setUserId(Integer.valueOf(userId.toString()));
            goodsLike.setGoodsId(Integer.valueOf(goodsId.toString()));
            goodsLike.setCreateTime(LocalDateTime.now());
            goodsLikeMapper.insert(goodsLikeMapper.selectById(goodsId));
            stringRedisTemplate.opsForSet().add(key, JSONUtil.toJsonStr(goodsLike));

        } else if (type==0) {
            LambdaQueryWrapper<GoodsLike> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(GoodsLike::getUserId, userId)
                    .eq(GoodsLike::getGoodsId, goodsId);
            goodsLikeMapper.delete(wrapper);
            stringRedisTemplate.opsForSet().remove(key, userId.toString());
        }


    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "goods.favorite.queue",durable = "true"),
            exchange =@Exchange(name = "goods.favorite.exchange",type = ExchangeTypes.DIRECT),
            key = {"goodsFavorite"}
    ))
    public void GoodsFavoritePublish(GoodsFavoriteMessage msg) {
        //获取商品id
        Long goodsId = msg.getGoodsId();
        //获取用户id
        Long userId = msg.getUserId();
        //获取type
        Integer type = msg.getType();

        //key
        String favoriteKey = GOODS_INFO_KEY + goodsId;

        //创建我们的实体类
        GoodsFavorite goodsFavorite = new GoodsFavorite();
        if(type==1){
            goodsFavorite.setUserId(Integer.valueOf(userId.toString()));
            goodsFavorite.setGoodsId(Integer.valueOf(goodsId.toString()));
            goodsFavorite.setCreateTime(LocalDateTime.now());
            goodsFavoriteMapper.insert(goodsFavoriteMapper.selectById(goodsId));
            stringRedisTemplate.opsForSet().add(favoriteKey, JSONUtil.toJsonStr(goodsFavorite));
        } else if (type==0) {
            LambdaQueryWrapper<GoodsFavorite> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(GoodsFavorite::getUserId, userId)
                    .eq(GoodsFavorite::getGoodsId, goodsId);
            goodsFavoriteMapper.delete(wrapper);
            stringRedisTemplate.opsForSet().remove(favoriteKey, userId);
        }}
}
