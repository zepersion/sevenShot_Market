package org.example.rewardservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;
import org.example.common.DTO.AllRewardDTO.PointsDTO;
import org.example.common.DTO.AllRewardDTO.RewardPublishDTO;
import org.example.common.DTO.AllRewardDTO.TaskListDTO;
import org.example.common.VO.GoodsAllVO.GoodsVO;
import org.example.common.VO.PageVO;
import org.example.common.VO.AIlRewardVO.PointsVO;
import org.example.common.VO.AIlRewardVO.RewardDetailVO;
import org.example.common.VO.AIlRewardVO.RewardVO;
import org.example.rewardservice.entity.Reward;

import java.util.List;

public interface RewardService extends IService<Reward> {

    RewardVO publish(@Valid RewardPublishDTO dto);


    PageVO<RewardVO> rewardList(Long id, TaskListDTO dto);



    RewardDetailVO detail(Long id);

    void apply(Long id, String msg);

    void selectBytakeId(Long taskId, Long acceptId);

    PageVO<RewardVO> myPublishList(Long userId, TaskListDTO dto);

    PageVO<RewardVO> myAcceptList(Long userId, TaskListDTO dto);

    PageVO<PointsVO> points(PointsDTO dto, Long userId);

    List<GoodsVO> getAiRecommendGoods(Long userId);
}
