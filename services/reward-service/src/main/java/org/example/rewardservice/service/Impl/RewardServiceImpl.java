package org.example.rewardservice.service.Impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.RewardPublishDTO;
import org.example.common.utils.UserHolder;
import org.example.common.vo.PageVO;
import org.example.common.vo.RewardVO;
import org.example.rewardservice.entity.Reward;
import org.example.rewardservice.mapper.RewardMapper;
import org.example.rewardservice.service.RewardService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RewardServiceImpl extends ServiceImpl<RewardMapper, Reward> implements RewardService {

    @Resource
    private RewardMapper rewardMapper;

    @Override
    @Transactional
    public RewardVO publish(RewardPublishDTO dto) {
        // TODO: 写发布逻辑
        // 1. 获取当前用户 UserHolder.getUser().getId()
        // 2. 组装 Reward 实体，images 从 List 转 String
        // 3. 设置 status=0（待接单），viewCount=0
        // 4. save
        // 5. 返回 convertToVO
        return null;
    }

    @Override
    public PageVO<RewardVO> list(Integer categoryId, Integer page, Integer size) {
        // TODO: 写列表查询逻辑
        // 1. 分页查询 status=0（待接单）的悬赏，可按 categoryId 过滤
        // 2. 转成 VO 列表返回
        return null;
    }

    @Override
    public RewardVO detail(Long id) {
        // TODO: 写详情查询逻辑
        // 1. getById
        // 2. viewCount+1（可以优化成 Redis INCR）
        // 3. 返回 convertToVO
        return null;
    }

    @Override
    public PageVO<RewardVO> myList(Integer page, Integer size) {
        // TODO: 写我的悬赏列表
        // 1. 查 publisherId = 当前用户
        // 2. 分页 + 转 VO
        return null;
    }

    @Override
    @Transactional
    public void accept(Long id) {
        // TODO: 写接单逻辑
        // 1. 查悬赏是否存在，状态是否=0（待接单）
        // 2. 不能接自己的悬赏
        // 3. 乐观锁更新：status 0→1，设置 acceptorId、acceptTime
        // 4. updateCount==0 抛异常
    }

    @Override
    @Transactional
    public void complete(Long id) {
        // TODO: 写完成逻辑
        // 1. 查悬赏是否存在，状态是否=1（进行中）
        // 2. 只有发布者能确认完成
        // 3. 乐观锁更新：status 1→2，设置 completeTime
        // 4. 后续可以发 MQ：扣赏金、加信用分、推送通知
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        // TODO: 写取消逻辑
        // 1. 查悬赏是否存在
        // 2. 只有发布者能取消，且状态必须=0（待接单才能取消）
        // 3. 乐观锁更新：status→3
    }

    private RewardVO convertToVO(Reward reward) {
        RewardVO vo = new RewardVO();
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
