package org.example.goodsservice.mq;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.Message.AllGoodsMsg.GoodsFavoriteMessage;
import org.example.common.Message.AllGoodsMsg.GoodsLikeMessage;
import org.example.goodsservice.FeignClient.AiFeignClient;
import org.example.goodsservice.entity.Goods;
import org.example.goodsservice.entity.GoodsFavorite;
import org.example.goodsservice.entity.GoodsLike;
import org.example.goodsservice.mapper.GoodsFavoriteMapper;
import org.example.goodsservice.mapper.GoodsLikeMapper;
import org.example.goodsservice.mapper.GoodsMapper;
import org.example.common.Message.AllGoodsMsg.GoodsPublishMQMessage;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static org.example.common.RedisConstants.GOODS_INFO_KEY;
import static org.example.common.RedisConstants.GOODS_LIKE_KEY;
@Slf4j
@Component
@RequiredArgsConstructor
public class GoodsConsumer {
    @Resource
    private GoodsFavoriteMapper goodsFavoriteMapper;
    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private GoodsLikeMapper goodsLikeMapper;
    @Resource
    private AiFeignClient aiFeignClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "goods.audit.queue",durable = "true"),
            exchange =@Exchange(name = "goods.direct",type = ExchangeTypes.DIRECT),
            key = {"goods.audit"}
    ))
    public void consumerGoodsPublish(GoodsPublishMQMessage msg) {


        String description = msg.getDescription();
        String title = msg.getTitle();
            Long id = msg.getId();
            Goods goods = goodsMapper.selectById(id);
            if(goods == null){
                log.error("商品不存在");
                return;
            }

            Boolean success= aiFeignClient.reviewPic(description, title);
     if(success){
         LambdaUpdateWrapper<Goods> updateWrapper = Wrappers.<Goods>lambdaUpdate()
                 .eq(Goods::getId, id)
                 .set(Goods::getDescription, description)
                 .set(Goods::getTitle, title)
                 .set(Goods::getStatus, 1);
         goodsMapper.update(updateWrapper);
         log.info("商品审核成功");
     }else {
         LambdaUpdateWrapper<Goods> updateWrapper = Wrappers.<Goods>lambdaUpdate()
                 .eq(Goods::getId, id)
                 .set(Goods::getStatus,4 );
         goodsMapper.update(updateWrapper);
         log.error("该商品违规!");
     }


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
            goodsLikeMapper.insert(goodsLike);
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
            goodsFavoriteMapper.insert(goodsFavorite);
            stringRedisTemplate.opsForSet().add(favoriteKey, JSONUtil.toJsonStr(goodsFavorite));
        } else if (type==0) {
            LambdaQueryWrapper<GoodsFavorite> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(GoodsFavorite::getUserId, userId)
                    .eq(GoodsFavorite::getGoodsId, goodsId);
            goodsFavoriteMapper.delete(wrapper);
            stringRedisTemplate.opsForSet().remove(favoriteKey, userId);
        }}
}
