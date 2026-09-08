package org.example.rewardservice.service.Impl;

import cn.hutool.json.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.RewardPublishDTO;
import org.example.common.dto.TaskListDTO;
import org.example.common.utils.UserHolder;
import org.example.common.vo.PageVO;
import org.example.common.vo.RewardDetailVO;
import org.example.common.vo.RewardVO;
import org.example.rewardservice.Task.DetailTask;
import org.example.rewardservice.entity.Reward;
import org.example.rewardservice.entity.TaskAccept;
import org.example.rewardservice.mapper.RewardMapper;
import org.example.rewardservice.mapper.TaskAcceptMapper;
import org.example.rewardservice.service.RewardService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.example.common.RedisConstants.REWARD_VIEW_KEY;
import static org.example.common.RedisConstants.REWARD_VIEW_USER_KEY;

@Slf4j
@Service
public class RewardServiceImpl extends ServiceImpl<RewardMapper, Reward> implements RewardService {

    @Resource
    private RewardMapper rewardMapper;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private TaskAcceptMapper taskAcceptMapper;
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
        if (reward.getStatus() != 0) {
            throw new RuntimeException("任务状态不允许选择接单人");
        }
       //接单记录表的修改
        LambdaUpdateWrapper<TaskAccept> taskacceptUpdate= new LambdaUpdateWrapper<>();
        LambdaUpdateWrapper<TaskAccept> updateOfTask = taskacceptUpdate.eq(TaskAccept::getId, acceptId)
                .eq(TaskAccept::getTaskId, taskId)
                .eq(TaskAccept::getStatus, 0)
                .set(TaskAccept::getStatus, 1);
        int update = taskAcceptMapper.update(null, updateOfTask);
        if(update == 0){
            throw new RuntimeException("记录不存在或者被处理");
        }
        //修改task的acceptorId
        TaskAccept taskAccept = taskAcceptMapper.selectById(acceptId);
        boolean update1 = lambdaUpdate().eq(Reward::getId, taskId)
                .eq(Reward::getStatus, 0)
                .set(Reward::getStatus, 1)
                .set(Reward::getAcceptorId, taskAccept.getAcceptorId())
                .set(Reward::getAcceptTime, LocalDateTime.now()).update();
        if(!update1){
            throw new RuntimeException("任务状态有问题,选择失败");
        }
        //其他id修改状态
        LambdaUpdateWrapper<TaskAccept> nochooesSet = new LambdaUpdateWrapper<TaskAccept>()
                .eq(TaskAccept::getTaskId, taskId)
                .eq(TaskAccept::getStatus, 0)
                .set(TaskAccept::getStatus, 2);
        taskAcceptMapper.update(null,nochooesSet);

    }

    @Override
    public RewardVO publish(RewardPublishDTO dto) {
        //拿到发布者:用户id
        Long id = UserHolder.getUser().getId();
        //
        if(id==null){
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
        Page<Reward> rewardPage = new Page<Reward>(dto.getPage(),dto.getSize());
        LambdaQueryWrapper<Reward> query = new LambdaQueryWrapper<>();
        query.eq(Reward::getStatus, 1)
                .and(dto.getKeyWord()!=null,wrapper->{
                    wrapper.eq(Reward::getPublisherId, id)
                            .like(Reward::getTitle, dto.getKeyWord())
                            .or().like(Reward::getDescription, dto.getKeyWord());
                }).le(dto.getMinReward()!=null,Reward::getReward,dto.getMinReward())
                .ge(dto.getMaxReward()!=null,Reward::getReward,dto.getMaxReward())
                .eq(dto.getTaskId()!=null,Reward::getId,dto.getTaskId())
                .eq(dto.getCategoryId()!=null,Reward::getCategoryId,dto.getCategoryId());
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
        if(userId != null){
             userViewKey = REWARD_VIEW_USER_KEY+userId;
        }
        String viewKey=REWARD_VIEW_KEY+id;

        if(userId==null||Boolean.FALSE.equals(stringRedisTemplate.hasKey(userViewKey))){
            if(userId!=null){
                stringRedisTemplate.opsForValue().set(userViewKey,userId.toString(),24,TimeUnit.HOURS);
            }
            stringRedisTemplate.opsForHash().increment(viewKey,id.toString(),1);
        }
        if(id==null){
            throw new RuntimeException("该任务状态异常");

        }

        Reward reward = rewardMapper.selectById(id);
        if(reward==null){
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
        if(task==null){
            throw new RuntimeException("该悬赏任务不存在");
        }
        if(task.getPublisherId().equals(accepthId)){
            throw new RuntimeException("不能申请自己发布的任务");
        }
       LambdaQueryWrapper<TaskAccept> taskAcceptQuery = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<TaskAccept> one= taskAcceptQuery.eq(TaskAccept::getTaskId, task.getId())
                .eq(TaskAccept::getAcceptorId, accepthId);
        TaskAccept taskAccept = taskAcceptMapper.selectOne(one);
        if(taskAccept!=null){
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
