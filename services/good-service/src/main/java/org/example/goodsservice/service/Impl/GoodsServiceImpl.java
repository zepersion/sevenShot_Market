package org.example.goodsservice.service.Impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.*;
import org.example.common.utils.UserHolder;
import org.example.common.vo.GoodsVO;
import org.example.common.vo.HotRankVO;
import org.example.common.vo.PageVO;
import org.example.goodsservice.FeignClient.UserFeignClient;
import org.example.goodsservice.entity.Goods;
import org.example.goodsservice.mapper.GoodsMapper;
import org.example.goodsservice.mq.GoodsProducer;
import org.example.common.Message.GoodsPublishMQMessage;
import org.example.goodsservice.service.GoodsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.example.common.RedisConstants.GOODS_INFO_KEY;
import static org.example.common.RedisConstants.GOODS_RANKS_HOT_KEY;

@Slf4j
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private GoodsProducer goodsProducer;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    
    
    @Override
    public GoodsVO publish(GoodsPublishDTO dto) {
        UserDto user = UserHolder.getUser();
        Long userId = user.getId();
        if(userId  == null) {
            throw new RuntimeException("用户未登录");
        }

        GoodsPublishMQMessage msg = new GoodsPublishMQMessage();
        msg.setUserId(userId);
        msg.setCategoryId(dto.getCategoryId());
        msg.setTitle(dto.getTitle());
        msg.setDegree(dto.getDegree());
        msg.setImages(Collections.singletonList(dto.getImages()));
        msg.setDescription(dto.getDescription());
        msg.setStock(dto.getStock());
        msg.setOriginalPrice(dto.getOriginalPrice());
        msg.setTags(Collections.singletonList(dto.getTags()));
        msg.setSellingPrice(dto.getSellingPrice());
        msg.setCoverImage(dto.getCoverImage());
        msg.setIsDonation(dto.getIsDonation());

        goodsProducer.goods_publish(msg);
        GoodsVO vo = new GoodsVO();

        vo.setStatus(0); // 待审核
        return vo;
    }


    @Override
    public PageVO<GoodsVO> goodsToList(GoodsListDTO dto, Integer page, Integer size) {
       //分页
        Page<Goods> goodsPage = new Page<>(page, size);
        QueryChainWrapper<Goods> goodsQuery = query().eq("status", 1).eq("categoryId", dto.getCategoryId()).like("keyword", dto.getKeyWord()).orderByDesc("sortType");
        Page<Goods> goodsInfo = baseMapper.selectPage(goodsPage, goodsQuery);
        List<Goods> goodsInfoList = goodsInfo.getRecords();

        //将enity转为vo
        List<GoodsVO> goodsVOList = goodsInfoList.stream()
                .map(goods ->
                        {
                            GoodsVO goodsVO = new GoodsVO();
                            BeanUtils.copyProperties(goods, goodsVO);
                            goodsVO.setId(goods.getId());
                            goodsVO.setTitle(goods.getTitle());
                            goodsVO.setStatus(goods.getStatus());
                            goodsVO.setCategoryId(goods.getCategoryId());
                            goodsVO.setCoverImage(goods.getCoverImage());
                            return goodsVO;
                        }
                ).collect(Collectors.toList());

       //封装
        PageVO<GoodsVO> pageVO = new PageVO<>();
        pageVO.setTotal(goodsInfo.getTotal());
        pageVO.setPages(goodsInfo.getPages());
        pageVO.setSize(goodsInfo.getSize());
        pageVO.setRecords(goodsVOList);

        return pageVO;
    }

    @Override
    public GoodsVO goodsDetail(GoodsDTO dto, Long id) {
        GoodsVO vo = new GoodsVO();
        String key=GOODS_INFO_KEY+id;
        String s = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(s)){
            JSONUtil.toJsonPrettyStr(s);
            GoodsVO bean = JSONUtil.toBean(s, GoodsVO.class);
            return bean;
        }

        Goods one = query().eq("id", id).one();

        if(one==null){
            throw new RuntimeException("没有该商品的信息");
        }

        BeanUtils.copyProperties(one, vo);
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(vo),30, TimeUnit.MINUTES);

        return vo;
    }

    @Override
    public PageVO<GoodsVO> myList(Integer page, Integer size) {
        //拿到我们的用户id
        Long id = UserHolder.getUser().getId();

        Page<Goods> goodsInfo = new Page<>(page, size);
        //查询:根据id查询并根据创建时间倒叙排序
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getId, id);
        wrapper.orderByDesc(Goods::getCreateTime);
        //分页查询
        Page<Goods> goodsPage=goodsMapper.selectPage(goodsInfo,wrapper);

        //取出信息
        List<Goods> goodsInfoList = goodsPage.getRecords();
        //将goodsInfoList转化成vo
        List<GoodsVO> collect = goodsInfoList.stream()
                .map(goods -> {
                    GoodsVO goodsVO = new GoodsVO();
                    BeanUtils.copyProperties(goods, goodsVO);
                    return goodsVO;
                }).collect(Collectors.toList());
        //组装vo
        PageVO<GoodsVO> vo = new PageVO<>();
        vo.setTotal(goodsInfo.getTotal());
        vo.setPages(goodsInfo.getPages());
        vo.setSize(goodsInfo.getSize());
        vo.setRecords(collect);
        return vo;
    }

    @Override
    public void offSaleGoods(Long goodsId) {

        //鉴权
        Long id = UserHolder.getUser().getId();
        //查询商品是否存在
        Goods goods = goodsMapper.selectById(goodsId);
        if(goods==null){
            log.error("该商品不存在");
            throw new RuntimeException("该商品不存在");
        }
        boolean equals = goods.getSellerId().equals(id);
        if(!equals){
            log.error("不能操作别人的商品");
            throw new RuntimeException("不能操作别人的商品");
        }
        if(goods.getStatus()==3){
            log.error("该商品已下架");
            throw new RuntimeException("该商品已下架");
        }
        update().eq("id", goodsId).eq("seller_id", id).set("status", 3).update();
    }

    @Override
    public List<HotRankVO> getHotRank(HotRankDTO dto) {
        //准备id集合和热度score
        List<Long> idList = new ArrayList<>();
        Map<Long,Double> scoreMap = new HashMap<>();
        //先从redis里面取出数据
        String key=GOODS_RANKS_HOT_KEY;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, 0, dto.getTop() - 1);
      //将我们的id 和对应的score分别取出,idlist用于遍历把我们的goods信息取出
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
           String value = typedTuple.getValue();
           long id = Long.parseLong(value);
           Double score = typedTuple.getScore();
           idList.add(id);
           scoreMap.put(id,score);
       }
        List<Goods> goodsList = goodsMapper.selectByIds(idList);
        if(goodsList==null||goodsList.isEmpty()){
            return Collections.emptyList();
        }
            //创建goodsEnityList
                    //创建集合
        Map<Long, Goods> goodsMap = goodsList.stream()
                .collect(Collectors
                        .toMap(Goods::getId, Function.identity()));
        //排序,将redis中不存在的数据进行剔除
        List<Goods> sortedList = idList.stream().map(goodsMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
       //categoryId内存过滤
        Long categoryId = dto.getCategoryId();
        if(categoryId!=null){
          sortedList=  sortedList.stream().filter(e->e.equals(categoryId)).collect(Collectors.toList());
        }
        List<HotRankVO> voList = new ArrayList<>();
        for(int i=0; i<sortedList.size(); i++){
            HotRankVO vo = new HotRankVO();
            vo.setId(sortedList.get(i).getId());
            vo.setGoodsPic(sortedList.get(i).getImages());
            vo.setGoodsName(sortedList.get(i).getTitle());
            vo.setRankNum(i+1);
            vo.setPrice(sortedList.get(i).getOriginalPrice());
            vo.setHotScore(scoreMap.get(sortedList.get(i).getId()));
            voList.add(vo);
        }
        return voList;
        }
}
