package org.example.rewardservice.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.common.DTO.AllRewardDTO.PointsDTO;
import org.example.common.DTO.AllRewardDTO.RewardPublishDTO;
import org.example.common.DTO.AllRewardDTO.TaskListDTO;
import org.example.common.Result;
import org.example.common.VO.GoodsAllVO.GoodsVO;
import org.example.common.utils.UserHolder;
import org.example.common.VO.PageVO;
import org.example.common.VO.AIlRewardVO.PointsVO;
import org.example.common.VO.AIlRewardVO.RewardDetailVO;
import org.example.common.VO.AIlRewardVO.RewardVO;
import org.example.rewardservice.FeignClient.AiFeignClient;
import org.example.rewardservice.FeignClient.GoodsFeignClient;
import org.example.rewardservice.entity.Reward;
import org.example.rewardservice.entity.TaskAccept;
import org.example.rewardservice.entity.TaskPointsRecord;
import org.example.rewardservice.mapper.RewardMapper;
import org.example.rewardservice.mapper.TaskAcceptMapper;
import org.example.rewardservice.mapper.TaskPointsRecordMapper;
import org.example.rewardservice.service.RewardService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.example.common.RedisConstants.*;

@Slf4j
@Service
public class RewardServiceImpl extends ServiceImpl<RewardMapper, Reward> implements RewardService {

    @Resource
    private RewardMapper rewardMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private TaskAcceptMapper taskAcceptMapper;
    @Resource
    private TaskPointsRecordMapper taskPointsRecordMapper;
    @Resource
    private   GoodsFeignClient goodsFeignClient;
    @Resource
    private AiFeignClient aiFeignClient;
    @Transactional
    @Override
    public void selectBytakeId(Long taskId, Long acceptId) {
        Long userId = UserHolder.getUser().getId();
        Reward reward = lambdaQuery()
                .eq(Reward::getId, taskId)
                .one();
        if (reward == null) {
            throw new RuntimeException("任务不存在");
        }
        if (!reward.getPublisherId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        if (reward.getStatus() != 1) {
            throw new RuntimeException("任务状态不允许选择接单人");
        }
        //接单记录表的修改
        LambdaUpdateWrapper<TaskAccept> taskacceptUpdate = new LambdaUpdateWrapper<>();
        LambdaUpdateWrapper<TaskAccept> updateOfTask = taskacceptUpdate.eq(TaskAccept::getId, acceptId)
                .eq(TaskAccept::getTaskId, taskId)
                .eq(TaskAccept::getStatus, 0)
                .set(TaskAccept::getStatus, 1);
        int update = taskAcceptMapper.update(null, updateOfTask);
        if (update == 0) {
            throw new RuntimeException("记录不存在或者被处理");
        }
        //修改task的acceptorId
        TaskAccept taskAccept = taskAcceptMapper.selectById(acceptId);
        boolean update1 = lambdaUpdate().eq(Reward::getId, taskId)
                .eq(Reward::getStatus, 1)
                .set(Reward::getStatus, 2)
                .set(Reward::getAcceptorId, taskAccept.getAcceptorId())
                .set(Reward::getAcceptTime, LocalDateTime.now()).update();
        if (!update1) {
            throw new RuntimeException("任务状态有问题,选择失败");
        }
        //其他id修改状态
        LambdaUpdateWrapper<TaskAccept> nochooesSet = new LambdaUpdateWrapper<TaskAccept>()
                .eq(TaskAccept::getTaskId, taskId)
                .eq(TaskAccept::getStatus, 0)
                .set(TaskAccept::getStatus, 2);
        taskAcceptMapper.update(null, nochooesSet);

    }

    @Override
    public RewardVO publish(RewardPublishDTO dto) {
        //拿到发布者:用户id
        Long id = UserHolder.getUser().getId();
        //
        if (id == null) {
            throw new RuntimeException("当前用户未登录");

        }
        Reward reward = new Reward();
        BeanUtils.copyProperties(dto, reward);
        reward.setPublisherId(id);
        reward.setStatus(0);  // 待接单
        reward.setViewCount(0);
        reward.setCreateTime(LocalDateTime.now());
        reward.setUpdateTime(LocalDateTime.now());

        save(reward);
        RewardVO vo = new RewardVO();
        BeanUtils.copyProperties(reward, vo);


        return vo;
    }

    @Override
    public PageVO<RewardVO> rewardList(Long id, TaskListDTO dto) {
        Page<Reward> rewardPage = new Page<Reward>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Reward> query = new LambdaQueryWrapper<>();
        query.eq(Reward::getStatus, 1)
                .eq(id != null, Reward::getPublisherId, id)
                .and(dto.getKeyWord() != null, wrapper -> {
                    wrapper.like(Reward::getTitle, dto.getKeyWord())
                            .or().like(Reward::getDescription, dto.getKeyWord());
                }).le(dto.getMinReward() != null, Reward::getReward, dto.getMaxReward())
                .ge(dto.getMaxReward() != null, Reward::getReward, dto.getMinReward())
                .eq(dto.getTaskId() != null, Reward::getId, dto.getTaskId())
                .eq(dto.getCategoryId() != null, Reward::getCategoryId, dto.getCategoryId());
        if (dto.getSortType() != null) {
            if (dto.getSortType() == 1) {
                query.orderByDesc(Reward::getReward);    // 赏金从高到低
            } else if (dto.getSortType() == 2) {
                query.orderByAsc(Reward::getReward);     // 赏金从低到高
            } else if (dto.getSortType() == 3) {
                query.orderByAsc(Reward::getCreateTime);// 最新发布
            }
        } else {
            query.orderByDesc(Reward::getCreateTime);// 默认按发布时间倒序
        }
        Page<Reward> pageList = rewardMapper.selectPage(rewardPage, query);
        List<RewardVO> voList = pageList.getRecords().stream().map(r ->
        {

            RewardVO vo = new RewardVO();
            BeanUtils.copyProperties(r, vo);
            return vo;
        }).collect(Collectors.toList());
        PageVO<RewardVO> vo = new PageVO<RewardVO>();
        vo.setTotal(pageList.getTotal());
        vo.setRecords(voList);
        vo.setSize(rewardPage.getSize());
        vo.setPages(rewardPage.getPages());
        vo.setTotal(pageList.getTotal());
        vo.setCurrent(pageList.getCurrent());
        return vo;
    }

    @Override
    public RewardDetailVO detail(Long id) {
        Long userId = UserHolder.getUser().getId();
        //userId可能为空（游客访问）
        String userViewKey = null;
        if (userId != null) {
            userViewKey = REWARD_VIEW_USER_KEY + userId;
        }
        String viewKey = REWARD_VIEW_KEY + id;

        if (userId == null || Boolean.FALSE.equals(stringRedisTemplate.hasKey(userViewKey))) {
            if (userId != null) {
                stringRedisTemplate.opsForValue().set(userViewKey, userId.toString(), 24, TimeUnit.HOURS);
            }
            stringRedisTemplate.opsForHash().increment(viewKey, id.toString(), 1);
        }
        if (id == null) {
            throw new RuntimeException("该任务状态异常");

        }

        Reward reward = rewardMapper.selectById(id);
        if (reward == null) {
            throw new RuntimeException("该任务不存在");
        }
        RewardDetailVO vo = new RewardDetailVO();
        BeanUtils.copyProperties(reward, vo);

        vo.setStatusName(getStatusName(reward.getStatus()));
        return vo;

    }

    @Transactional
    @Override
    public void apply(Long id, String msg) {
        Long accepthId = UserHolder.getUser().getId();
        //publishid 不能和申请id一致
        Reward task = lambdaQuery().eq(Reward::getId, id).one();
        if (task == null) {
            throw new RuntimeException("该悬赏任务不存在");
        }
        if (task.getPublisherId().equals(accepthId)) {
            throw new RuntimeException("不能申请自己发布的任务");
        }
        LambdaQueryWrapper<TaskAccept> taskAcceptQuery = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<TaskAccept> one = taskAcceptQuery.eq(TaskAccept::getTaskId, task.getId())
                .eq(TaskAccept::getAcceptorId, accepthId);
        TaskAccept taskAccept = taskAcceptMapper.selectOne(one);
        if (taskAccept != null) {
            throw new RuntimeException("已经申请过该任务");
        }
        TaskAccept acceptstatus = new TaskAccept();
        acceptstatus.setTaskId(task.getId());
        acceptstatus.setAcceptorId(accepthId);
        acceptstatus.setStatus(0);
        acceptstatus.setCreateTime(LocalDateTime.now());
        acceptstatus.setMessage(msg);
        taskAcceptMapper.insert(acceptstatus);


    }


    @Override
    public PageVO<RewardVO> myPublishList(Long userId, TaskListDTO dto) {
        Integer page = dto.getPage() != null ? dto.getPage() : 1;
        Integer size = dto.getSize() != null ? dto.getSize() : 10;
        Page<Reward> pages = new Page<>(page, size);
        LambdaQueryWrapper<Reward> query = new LambdaQueryWrapper<>();
        query.eq(Reward::getPublisherId, userId)
                .and(dto.getKeyWord() != null, wrapper -> {
                    wrapper.like(Reward::getTitle, dto.getKeyWord())
                            .or().like(Reward::getDescription, dto.getKeyWord());
                })
                .le(dto.getMinReward() != null, Reward::getReward, dto.getMinReward())
                .ge(dto.getMaxReward() != null, Reward::getReward, dto.getMaxReward())
                .eq(dto.getCategoryId() != null, Reward::getCategoryId, dto.getCategoryId());
        if (dto.getSortType() != null) {
            if (dto.getSortType() == 1) {
                query.orderByDesc(Reward::getReward);
            } else if (dto.getSortType() == 2) {
                query.orderByAsc(Reward::getReward);
            } else if (dto.getSortType() == 3) {
                query.orderByAsc(Reward::getCreateTime);
            }
        } else {
            query.orderByDesc(Reward::getCreateTime);
        }
        Page<Reward> rewardPage = rewardMapper.selectPage(pages, query);
        List<RewardVO> collect = rewardPage.getRecords().stream().map(r -> {
            RewardVO vo = new RewardVO();
            BeanUtils.copyProperties(r, vo);
            return vo;
        }).collect(Collectors.toList());
        PageVO<RewardVO> vo = new PageVO<>();
        vo.setTotal(rewardPage.getTotal());
        vo.setRecords(collect);
        vo.setSize(rewardPage.getSize());
        vo.setPages(rewardPage.getPages());
        vo.setCurrent(rewardPage.getCurrent());
        return vo;
    }

    @Override
    public PageVO<RewardVO> myAcceptList(Long userId, TaskListDTO dto) {
        Integer page = dto.getPage() != null ? dto.getPage() : 1;
        Integer size = dto.getSize() != null ? dto.getSize() : 10;


        Page<Reward> pages = new Page<>(page, size);
        //从accept拿数据
        LambdaQueryWrapper<TaskAccept> accepterQuery = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<TaskAccept> accepters = accepterQuery.eq(TaskAccept::getAcceptorId, userId);
        List<TaskAccept> taskAccepts = taskAcceptMapper.selectList(accepters);
        //判断如果为空 返回空集合
        if (BeanUtil.isEmpty(taskAccepts)) {
            PageVO<RewardVO> vo = new PageVO<>();
            vo.setCurrent(0L);
            vo.setSize(0L);
            vo.setTotal(0L);
            vo.setRecords(Collections.emptyList());
            return vo;
        }
        //根据这些资料通过taskid查询Reward表
        List<Long> ids = taskAccepts.stream().map(TaskAccept::getTaskId).toList();
        //
        Page<Reward> pageEnity = this.lambdaQuery().in(Reward::getId, ids)
                .like(Reward::getTitle, dto.getKeyWord())
                .like(Reward::getDescription, dto.getKeyWord())
                .like(Reward::getReward, dto.getKeyWord())
                .like(Reward::getCategoryId, dto.getCategoryId())
                .orderByDesc(Reward::getCreateTime)
                .page(new Page<>(page, size));//条件
        //拿出RewardVO的集合
        List<RewardVO> collect = pageEnity.getRecords().stream().map(reward -> {
            RewardVO vo = new RewardVO();
            BeanUtils.copyProperties(reward, vo);
            return vo;
        }).collect(Collectors.toList());

        // 拼接vo
        PageVO<RewardVO> vo = new PageVO<>();
        vo.setTotal(pageEnity.getTotal());
        vo.setRecords(collect);
        vo.setSize(pageEnity.getSize());
        vo.setPages(pageEnity.getPages());
        vo.setCurrent(pageEnity.getCurrent());
        return vo;

    }

    @Override
    public PageVO<PointsVO> points(PointsDTO dto, Long userId) {
        Integer page = dto.getPage() != null ? dto.getPage() : 1;
        Integer size = dto.getSize() != null ? dto.getSize() : 10;


        LambdaQueryWrapper<TaskPointsRecord> pointsQuery = new LambdaQueryWrapper<>();
        pointsQuery.eq(TaskPointsRecord::getUserId, userId)
                .orderByDesc(TaskPointsRecord::getCreateTime);

        if (dto.getType() != null) {
            pointsQuery.eq(TaskPointsRecord::getType, dto.getType());
        }
        Page<TaskPointsRecord> pages = new Page<>(page, size);
        Page<TaskPointsRecord> recordPage = taskPointsRecordMapper.selectPage(pages, pointsQuery);


        List<PointsVO> voList = recordPage.getRecords().stream().map(record -> {
            PointsVO vo = new PointsVO();

            BeanUtils.copyProperties(record, vo);

            return vo;
        }).collect(Collectors.toList());
        PageVO<PointsVO> result = new PageVO<>();
        result.setPages(recordPage.getPages());
        result.setSize(recordPage.getSize());
        result.setTotal(recordPage.getTotal());
        result.setPages(recordPage.getPages());
        result.setRecords(voList);

        return result;


    }

    @Override
    public List<GoodsVO> getAiRecommendGoods(Long userId) {
        if (userId == null) {
            return null;
        }
        Reward one = lambdaQuery().eq(Reward::getPublisherId, userId)
                .eq(Reward::getStatus, 1)
                .orderByDesc(Reward::getCreateTime)
                .one();
        //
    String cache=AI_RECOMMEND_KEY+one.getTitle()+":"+one.getDescription();
        String aiRecomend = stringRedisTemplate.opsForValue().get(cache);
        if(aiRecomend!=null){
            return JSONUtil.toList(aiRecomend, GoodsVO.class);
    }
        if (one == null) {
            return goodsFeignClient.listValidGoods().getData();
        }

        String text=one.getTitle()+":"+one.getDescription();
        Result<List<GoodsVO>> remoteResult = goodsFeignClient.listValidGoods();
        List<GoodsVO> goodsList = remoteResult.getData();
        if(CollectionUtils.isEmpty(goodsList)){
            return new ArrayList<>();
        }
        String result = aiFeignClient.recommend(text);
        stringRedisTemplate.opsForValue().set(cache, result, 30, TimeUnit.MINUTES);
        return JSONUtil.toList(result, GoodsVO.class);
    }


    private String getStatusName(Integer status) {
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "待接单";
            case 2 -> "进行中";
            case 3 -> "待确认";
            case 4 -> "已完成";
            case 5 -> "已取消";
            case 6 -> "违规";
            default -> "未知";
        };
    }


    }
