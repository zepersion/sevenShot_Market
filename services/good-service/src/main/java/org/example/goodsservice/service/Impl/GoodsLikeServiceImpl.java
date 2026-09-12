package org.example.goodsservice.service.Impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.common.Message.AllGoodsMsg.GoodsLikeMessage;
import org.example.common.dto.AllGoodsDTO.GoodLikeDTO;
import org.example.common.utils.UserHolder;
import org.example.goodsservice.entity.Goods;
import org.example.goodsservice.entity.GoodsLike;
import org.example.goodsservice.mapper.GoodsLikeMapper;
import org.example.goodsservice.mapper.GoodsMapper;
import org.example.goodsservice.service.GoodsLikeService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static org.example.common.RedisConstants.GOODS_INFO_KEY;
import static org.example.common.RedisConstants.GOODS_LIKE_KEY;

@Slf4j
@Service
public class GoodsLikeServiceImpl extends ServiceImpl<GoodsLikeMapper, GoodsLike> implements GoodsLikeService  {
    @Resource
    private GoodsLikeMapper goodsLikeMapper;
    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Override
    public void isLike(GoodLikeDTO goodsLikeDTO) {
        //消息
        GoodsLikeMessage like = new GoodsLikeMessage();
        //获取传入的值
        Integer id = goodsLikeDTO.getGoodsId();
        //获取用户id
        Long userId = UserHolder.getUser().getId();

        String key=GOODS_LIKE_KEY+id;
        String goodskey=GOODS_INFO_KEY+id;
        String s = stringRedisTemplate.opsForValue().get(goodskey);
        if(StrUtil.isBlank(s)){
            Goods goods = goodsMapper.selectById(id);
            if(goods==null){
                log.error("该商品不存在或已下架");
                return;
            }else
                stringRedisTemplate.opsForValue().set(goodskey,JSONUtil.toJsonStr(goods));
        }



        Boolean member = stringRedisTemplate.opsForSet().isMember(key, userId);
        if(Boolean.TRUE.equals(member)){
            //0为取关 1为关注
            like.setType(0);
        }else {

            like.setType(1);
        }

        rabbitTemplate.convertAndSend("goodsLikeExchange","goodsLike",like);
    }
}
