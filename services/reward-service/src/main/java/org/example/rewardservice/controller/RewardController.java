package org.example.rewardservice.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.common.Result;
import org.example.common.dto.RewardPublishDTO;
import org.example.common.dto.TaskListDTO;
import org.example.common.utils.UserHolder;
import org.example.common.vo.PageVO;
import org.example.common.vo.RewardDetailVO;
import org.example.common.vo.RewardVO;
import org.example.rewardservice.service.RewardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reward")
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
    public Result rewardList(@RequestBody TaskListDTO dto) {
        Long id = UserHolder.getUser().getId();
        PageVO<RewardVO> vo = rewardService.rewardList(id, dto);
        return Result.success(vo);
    }
    //    5.3
    @GetMapping("/task/{id}")
    public Result detail(@RequestParam Long id){
        RewardDetailVO vo = rewardService.detail(id);
        return Result.success(vo);
    }
    //5.4
    @PostMapping("/apply/{id}")
    public Result apply(@PathVariable Long id,@RequestBody String msg){
        rewardService.apply(id,msg);
        return Result.success();
    }
}