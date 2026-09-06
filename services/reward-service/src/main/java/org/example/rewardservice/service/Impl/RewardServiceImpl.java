package org.example.rewardservice.service.Impl;

import cn.hutool.json.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.example.rewardservice.mapper.RewardMapper;
import org.example.rewardservice.service.RewardService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
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
    private DetailTask detailTask;
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
        reward.setTaskPublisherId(id);
        reward.setStatus(0);  // 待接单
        reward.setViewCount(0);
        reward.setCreateTime(LocalDateTime.now());
        reward.setUpdateTime(LocalDateTime.now());
        if (dto.getTaskImages() != null && !dto.getTaskImages().isEmpty()) {
            reward.setImages(String.join(",", dto.getTaskImages()));
        }
      save(reward);
      RewardVO vo = new RewardVO();
      BeanUtils.copyProperties(reward, vo);
        if (reward.getImages() != null && !reward.getImages().isBlank()) {
            vo.setTaskImages(Arrays.asList(reward.getImages().split(",")));
        }
        vo.setTaskStatusName("待接单");
        return vo;
    }

    @Override
    public PageVO<RewardVO> rewardList(Long id, TaskListDTO dto) {
        Page<Reward> rewardPage = new Page<Reward>(dto.getPage(),dto.getSize());
        LambdaQueryWrapper<Reward> query = new LambdaQueryWrapper<>();
        query.eq(id!=null,Reward::getTaskPublisherId, id)
                .eq(Reward::getStatus, 0)
                .and(dto.getKeyWord()!=null,wrapper->{
                    wrapper.eq(Reward::getTaskPublisherId, id)
                            .like(Reward::getTitle, dto.getKeyWord())
                            .or().like(Reward::getDescription, dto.getKeyWord());
                }).le(dto.getMinReward()!=null,Reward::getRewardAmount,dto.getMinReward())
                .ge(dto.getMaxReward()!=null,Reward::getRewardAmount,dto.getMaxReward())
                .eq(dto.getTaskId()!=null,Reward::getId,dto.getTaskId())
                .eq(dto.getCategoryId()!=null,Reward::getCategoryId,dto.getCategoryId());
        if (dto.getSortType() != null) {
            if (dto.getSortType() == 1) {
                query.orderByDesc(Reward::getRewardAmount);    // 赏金从高到低
            } else if (dto.getSortType() == 2) {
                query.orderByAsc(Reward::getRewardAmount);     // 赏金从低到高
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
        RewardDetailVO vo = new RewardDetailVO();
        BeanUtils.copyProperties(reward, vo);
        if (reward.getImages() != null && !reward.getImages().isBlank()) {
            vo.setImages(Arrays.asList(reward.getImages().split(",")));
        }
        vo.setStatusName(getStatusName(reward.getStatus()));
        return vo;

    }
    private String getStatusName(Integer status) {
        return switch (status) {
            case 0 -> "待接单";
            case 1 -> "进行中";
            case 2 -> "已完成";
            case 3 -> "已取消";
            default -> "未知";
        };
    }

}
