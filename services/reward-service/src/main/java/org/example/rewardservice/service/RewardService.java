package org.example.rewardservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;
import org.example.common.dto.AllRewardDTO.PointsDTO;
import org.example.common.dto.AllRewardDTO.RewardPublishDTO;
import org.example.common.dto.AllRewardDTO.TaskListDTO;
import org.example.common.vo.GoodsAllVO.GoodsVO;
import org.example.common.vo.PageVO;
import org.example.common.vo.AIlRewardVO.PointsVO;
import org.example.common.vo.AIlRewardVO.RewardDetailVO;
import org.example.common.vo.AIlRewardVO.RewardVO;
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
