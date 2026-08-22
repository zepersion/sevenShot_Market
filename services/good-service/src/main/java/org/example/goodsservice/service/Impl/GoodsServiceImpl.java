package org.example.goodsservice.service.Impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.example.common.Message.SeckillOrderMessage;
import org.example.common.dto.*;
import org.example.common.utils.UserHolder;
import org.example.common.vo.*;
import org.example.goodsservice.FeignClient.UserFeignClient;
import org.example.goodsservice.entity.Goods;
import org.example.goodsservice.mapper.GoodsMapper;
import org.example.goodsservice.mq.GoodsProducer;
import org.example.common.Message.GoodsPublishMQMessage;
import org.example.goodsservice.service.GoodsService;
import org.redisson.api.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.example.common.RedisConstants.*;

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
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    private  static final   DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        //读取resources下的lua脚本文件
        Resource resource = new ClassPathResource("seckill.lua");
        SECKILL_SCRIPT.setLocation(resource);
        SECKILL_SCRIPT.setResultType(Long.class);
    }
    
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
//改造该功能利用redisson防止缓存雪崩

    @Override
    public GoodsVO goodsDetail( Long id) throws InterruptedException {
        GoodsVO vo = new GoodsVO();
        //key
        String key=GOODS_INFO_KEY+id;
        String lockKey = LOCK_GOODS_KEY+id;

        //如果命中则不加锁
        String s = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(s)){
            if("{}".equals(s)){
                log.error("该商品不存在");
                return null;
            }
            GoodsVO bean = JSONUtil.toBean(s, GoodsVO.class);
            return bean;
        }
//未命中
        Goods goods = new Goods();
        RLock lock = redissonClient.getLock(lockKey);
        try {

            boolean isLock= lock.tryLock(10, TimeUnit.SECONDS);
            //为获取锁
            if(! isLock){
                throw new RuntimeException("访问人数过多,请稍后再试");
            }
             s = stringRedisTemplate.opsForValue().get(key);
            if(StrUtil.isNotBlank(s)){
                if("{}".equals(s)){
                    log.error("该商品不存在");
                    return null;
                }
                GoodsVO bean = JSONUtil.toBean(s, GoodsVO.class);
                return bean;
            }//双重验证
            //查数据库
            goods= query().eq("id", id).one();
            if (goods == null) {
                stringRedisTemplate.opsForValue().set(key, "{}", 10, TimeUnit.MINUTES);
                throw new RuntimeException("没有该商品的信息");
            }

            BeanUtils.copyProperties(goods, vo);
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(vo), 30, TimeUnit.MINUTES);
        }finally {
                if(lock.isHeldByCurrentThread()){
                    lock.unlock();
                }
            }
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

    @Override
    public SeckillGoodsDataVO seckillList() {
        //vo
        SeckillGoodsDataVO vo = new SeckillGoodsDataVO();
        SeckillGoodsObjectVO objectVO = new SeckillGoodsObjectVO();
        //key
        String listKey=SECKILL_LIST_KEY;

        String s = stringRedisTemplate.opsForValue().get(listKey);
        if(s!=null && !"".equals(s)&&!s.isBlank()){
            vo = JSONUtil.toBean(s,SeckillGoodsDataVO.class);

            return vo;
        }

        List<Goods> goodsList = lambdaQuery()
                .eq(Goods::getIsSeckill, 1)
                .list();
        List<SeckillGoodsObjectVO> objlist = goodsList.stream().map(goods ->
                {String soldKey=SECKILL_SOLDCOUNT_KEY+goods.getId();
                    Long count = stringRedisTemplate.opsForValue().increment(soldKey,1);
                    objectVO.setGoodsId(goods.getId());
                    objectVO.setTitle(goods.getTitle());
                    objectVO.setCoverImage(goods.getCoverImage());
                    objectVO.setOriginalPrice(goods.getOriginalPrice());
                    objectVO.setSoldCount(count);
                    objectVO.setSeckillStock(goods.getSeckillStock());
                    objectVO.setSeckillPrice(goods.getSeckillPrice());
                    return objectVO;
                }
        ).collect(Collectors.toList());
                vo.setSeckillStartTime(objlist.get(0).getStartTime());
                vo.setSeckillEndTime(objlist.get(0).getEndTime());
                    vo.setSeckillGoodsList(objlist);
        return vo;
    }

    @Override
    public void seckill(Long goodsId, SeckillSaleDTO dto) {
         //这里有新方法 我们需要拿出当前用户的id 然后通过对比我们user的秒杀商品里是否有当前商品
        Long userId = UserHolder.getUser().getId();
        if(userId==null){
            log.error("该用户未登录");
            throw   new RuntimeException("用户未登录 请先登录");
        }
        //限流用redisson的令牌桶
        // 参数：速率、时间单位、令牌桶最大容量
// 每秒放行20个，桶最大30
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(SECKILL_LIMITER+goodsId);
        boolean b = rateLimiter.trySetRate(RateType.OVERALL, 20, 1, RateIntervalUnit.MILLISECONDS);
        if(!b){
            log.error("请求太频繁,请稍后重试");
            return ;
        }//取我们需要的key
        String goodsinfoKey=SECKILL_INFO_KEY+"{" + goodsId + "}";//这是我们取hash里面的值所需要的key
        String stockKey=SECKILL_STOCK_KEY+"{" + goodsId + "}";//lua脚本我们需要的
        String seckillStartTimeKey=SECKILL_STARTTIME_KEY+"{" + goodsId + "}";
        String seckillEndTimeKey=SECKILL_ENDTTIME_KEY+"{" + goodsId + "}";
        //接下来是lua脚本里面需要的 :分别取商品的用户购买的key以及我们用户的id;
        String userSetKey= SECKILL_USER_SET_KEY+goodsId;
        String user=userId.toString();

            //取缓存的东西
        //取时间
        Object startTime = stringRedisTemplate.opsForHash().get(goodsinfoKey, seckillStartTimeKey);
        Object endTime = stringRedisTemplate.opsForHash().get(goodsinfoKey, seckillEndTimeKey);
        LocalDateTime start = LocalDateTime.parse(startTime.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime end = LocalDateTime.parse(endTime.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        //组装我们的秒杀用户集合 和我们的库存 因为lua脚本识别的话用argv
        List<String> argvs = Arrays.asList(stockKey,userSetKey);
        //判断时间
        if(start.isAfter(LocalDateTime.now())){
                log.error("活动还没开始");
                return ;
            }
            if(end.isBefore(LocalDateTime.now())){
                log.error("活动已经结束");
                return ;
            }

        Long execute = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                argvs
                , user
        );
        if(execute==null){
            log.error("系统错误,请联系管理员");
            throw new RuntimeException("系统错误");
        }
        if(execute == -1)
        {
            log.error("库存不足,无法创造订单");
            return ;
        }
        if(execute == 0){
            log.error("用户无法重复下单");
        }
        SeckillOrderMessage msg = new SeckillOrderMessage();
        msg.setGoodsId(goodsId);
        msg.setAddress(dto.getAddress());
        msg.setMessage(dto.getMsg());
        msg.setQuantity(dto.getQuantity());
        msg.setTradeWay(dto.getTradeWay());
        rabbitTemplate.convertAndSend("order-exchange","order.seckill",msg);

    }

    @Override
    public void SeckillPreload() {

        List<Goods> list = lambdaQuery().eq(Goods::getIsSeckill, 1).list();
        List<Long> collect = list.stream().map(Goods::getId).collect(Collectors.toList());
        List<Goods> goods = goodsMapper.selectByIds(collect);
        //预热需要三个key
        for(Goods good:goods){
            Long goodsId = good.getId();
            String goodsinfoKey=SECKILL_INFO_KEY+"{" + goodsId + "}";
            String goodsStockKey=SECKILL_STOCK_KEY+"{" + goodsId + "}";
            String seckillStartTimeKey=SECKILL_STARTTIME_KEY+"{" + goodsId + "}";
            String seckillEndTimeKey=SECKILL_ENDTTIME_KEY+"{" + goodsId + "}";

            stringRedisTemplate.opsForHash().put(goodsinfoKey,seckillStartTimeKey,good.getSeckillStart());
            stringRedisTemplate.opsForHash().put(goodsinfoKey,seckillEndTimeKey,good.getSeckillEnd());
           Long hour = new Random().nextLong(3);
            Long expireTime = 24+hour;
            stringRedisTemplate.expire(goodsinfoKey, Duration.ofHours(expireTime));

            stringRedisTemplate.opsForValue().set(goodsStockKey,good.getSeckillStock().toString());
        }






    }
}

