package org.example.rewardservice.controller;

import jakarta.annotation.Resource;
import org.example.common.Result;
import org.example.common.dto.AllRewardDTO.PointsDTO;
import org.example.common.dto.AllRewardDTO.RewardPublishDTO;
import org.example.common.dto.AllRewardDTO.TaskApplyDTO;
import org.example.common.dto.AllRewardDTO.TaskListDTO;
import org.example.common.vo.GoodsAllVO.GoodsVO;
import org.example.common.utils.UserHolder;
import org.example.common.vo.PageVO;
import org.example.common.vo.AIlRewardVO.PointsVO;
import org.example.common.vo.AIlRewardVO.RewardDetailVO;
import org.example.common.vo.AIlRewardVO.RewardVO;
import org.example.rewardservice.service.RewardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task")
public class RewardController {

    @Resource
    private RewardService rewardService;

    //5.1
    @PostMapping("/publish")
    public Result publish(@RequestBody RewardPublishDTO DTO) {
        RewardVO vo = rewardService.publish(DTO);
        return Result.success(vo, "发布成功");
    }

    //5.2
    @GetMapping("/list")
    public Result rewardList( TaskListDTO dto) {
        Long id = UserHolder.getUser().getId();
        PageVO<RewardVO> vo = rewardService.rewardList(id, dto);
        return Result.success(vo);
    }
    //    5.3
    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id){
        RewardDetailVO vo = rewardService.detail(id);
        return Result.success(vo);
    }
    //5.4
    @PostMapping("/apply/{id}")
    public Result apply(@PathVariable Long id, @RequestBody TaskApplyDTO dto){
        rewardService.apply(id, dto.getMessage());
        return Result.success();
    }
    //5.5发布者选择接单人
    @PostMapping("/select/{taskId}/{acceptId}")
    public Result selectAccept(@PathVariable Long taskId,@PathVariable Long acceptId){
        rewardService.selectBytakeId(taskId,acceptId);
        return Result.success();
    }
    //5.6
    @GetMapping("/my/publish")
    public Result myPublish(TaskListDTO dto){
        Long id = UserHolder.getUser().getId();
        PageVO<RewardVO> vo=rewardService.myPublishList(id, dto);
        return Result.success(vo);
    }
    //5.7
    @GetMapping("/my/accept")
    public Result myAccept(TaskListDTO dto){
        Long id = UserHolder.getUser().getId();
        PageVO<RewardVO> vo=rewardService.myAcceptList(id, dto);
        return Result.success(vo);
    }
    //5.8
    @GetMapping("/recommend")
    public Result<List<GoodsVO>> recommend(){
        Long userId = UserHolder.getUser().getId();
        List<GoodsVO> vo=rewardService.getAiRecommendGoods(userId);
        return Result.success(vo);
    }
    //5.9
    @GetMapping("/points/records")
    public Result records(PointsDTO dto){
        Long userId = UserHolder.getUser().getId();
    PageVO<PointsVO> vo = rewardService.points(dto,userId);
     return Result.success(vo);
    }
}