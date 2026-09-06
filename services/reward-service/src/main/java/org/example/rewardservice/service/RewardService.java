package org.example.rewardservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;
import org.example.common.dto.RewardPublishDTO;
import org.example.common.dto.TaskListDTO;
import org.example.common.vo.PageVO;
import org.example.common.vo.RewardDetailVO;
import org.example.common.vo.RewardVO;
import org.example.rewardservice.entity.Reward;

public interface RewardService extends IService<Reward> {

    RewardVO publish(@Valid RewardPublishDTO dto);


    PageVO<RewardVO> rewardList(Long id, TaskListDTO dto);

    RewardDetailVO detail(Long id);
}
