package org.example.goodsservice.service.Impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.common.Message.AllGoodsMsg.GoodsFavoriteMessage;
import org.example.common.dto.AllGoodsDTO.GoodsFavoriteDTO;
import org.example.common.utils.UserHolder;
import org.example.common.vo.GoodsAllVO.GoodsVO;
import org.example.common.vo.PageVO;
import org.example.goodsservice.entity.Goods;
import org.example.goodsservice.entity.GoodsFavorite;
import org.example.goodsservice.mapper.GoodsFavoriteMapper;
import org.example.goodsservice.mapper.GoodsMapper;
import org.example.goodsservice.service.GoodsFavoriteService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.example.common.RedisConstants.GOODS_FAVORITE_KEY;
import static org.example.common.RedisConstants.GOODS_FAVORITE_SET;

@Service
@Slf4j
public class GoodsFavoriteServiceImpl extends ServiceImpl<GoodsFavoriteMapper, GoodsFavorite> implements GoodsFavoriteService {

   @Resource
   private GoodsFavoriteMapper goodsFavoriteMapper;


   @Resource
   private RabbitTemplate rabbitTemplate;
   @Resource
   private StringRedisTemplate stringRedisTemplate;
    @Resource
    private GoodsMapper goodsMapper;

    @Override
    public void isFavorite(GoodsFavoriteDTO goodFavoriteDTO) {
        //取用户和商品id
        Integer goodsId = goodFavoriteDTO.getGoodsId();
        Long userId = UserHolder.getUser().getId();
        //key
        String favoriteKey = GOODS_FAVORITE_SET + goodsId;

        String goodsinfo = stringRedisTemplate.opsForValue().get(favoriteKey);
        //msg
        GoodsFavoriteMessage msg = new GoodsFavoriteMessage();
        //先查缓存
        if (goodsinfo == null) {
            Goods goods = goodsMapper.selectById(goodsId);
            if (goods == null) {
                log.warn("商品编号为:{}该商品不存在或已下架", goodsId);
                return;
            } else stringRedisTemplate.opsForValue().set(favoriteKey, JSONUtil.toJsonStr(goods));

        }
        Boolean member = stringRedisTemplate.opsForSet().isMember(favoriteKey, userId);
        if (BooleanUtil.isTrue(member)) {

            msg.setType(0);
        } else {

            msg.setType(1);
        }
        rabbitTemplate.convertAndSend("goodsFavoriteExchange", "goodsFavorite", msg);
    }
//TODO业务较复杂 需要写完后再看
    @Override
    public PageVO<GoodsVO> goodsFavoriteToList(GoodsFavoriteDTO dto, Integer page, Integer size) {
        PageVO<GoodsVO> vo = new PageVO<>();
        //拿到当前登录用户 userId（UserHolder）
        Long id = UserHolder.getUser().getId();
        //拼接 Redis ZSet key：user:favorite:zset:userId
        String favoriteKey = GOODS_FAVORITE_KEY + id;
      //  根据 page、size 算出起止下标 start、end
       Long start = (long) ((page - 1) * size);
       Long end   = (long) (page * size - 1);

    //ZSet.reverseRange (start,end) → 拿到当前页有序商品 id 字符串集合
        Set<String> strings = stringRedisTemplate.opsForZSet().reverseRange(favoriteKey, start, end);
        //  zCard () 获取总收藏数量 total
        Long total = stringRedisTemplate.opsForZSet().zCard(favoriteKey);
        Long pages = total % size == 0 ? total / size : total / size + 1;
    /*如果没有收藏，直接返回空分页
把字符串 id 转成 Long 集合
批量根据 id 查询商品 selectBatchIds
重点：不能直接遍历数据库查到的商品！
数据库 in 查询返回顺序乱掉！*/
      vo.setTotal(total);
      vo.setSize(Long.valueOf(size));
      vo.setPages(Long.valueOf(pages));
      vo.setCurrent(Long.valueOf(page));
//必须循环【ZSet 返回的有序 id 列表】，逐个匹配商品，保证顺序不变
        //没有收藏数据
        if (total == 0 || CollectionUtil.isEmpty(strings)) {
            vo.setRecords(Collections.emptyList());
            return vo;
        }

        //字符串id转Long集合
        List<Long> goodsIdList = strings.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        //批量查询商品
        List<Goods> goodsList = goodsMapper.selectBatchIds(goodsIdList);
        Map<Long, Goods> goodsMap = goodsList.stream()
                .collect(Collectors.toMap(Goods::getId, g -> g));

        // 【重点】按照ZSet原有顺序组装VO，保证顺序不乱
        List<GoodsVO> voList = new ArrayList<>();
        for (Long gid : goodsIdList) {
            Goods goods = goodsMap.get(gid);
            GoodsVO goodsVO = new GoodsVO();
            if (goods != null) {
                BeanUtils.copyProperties(goods, goodsVO);
            } else {
                goodsVO.setId(gid);
                goodsVO.setTitle("商品已删除");
                goodsVO.setStatus(3);
            }
            voList.add(goodsVO);
        }
        vo.setRecords(voList);
        return vo;
    }
}
