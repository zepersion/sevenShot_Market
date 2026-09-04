package org.example.rewardservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;
import org.example.common.dto.RewardPublishDTO;
import org.example.common.vo.PageVO;
import org.example.common.vo.RewardVO;
import org.example.rewardservice.entity.Reward;

public interface RewardService extends IService<Reward> {

    RewardVO publish(@Valid RewardPublishDTO dto);

    PageVO<RewardVO> list(Integer categoryId, Integer page, Integer size);

    RewardVO detail(Long id);

    PageVO<RewardVO> myList(Integer page, Integer size);

    void accept(Long id);

    void complete(Long id);

    void cancel(Long id);
}
