package org.example.rewardservice.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.common.Result;
import org.example.common.dto.RewardPublishDTO;
import org.example.common.vo.PageVO;
import org.example.common.vo.RewardVO;
import org.example.rewardservice.service.RewardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reward")
public class RewardController {

    @Resource
    private RewardService rewardService;

    @PostMapping("/publish")
    public Result publish(@RequestBody @Valid RewardPublishDTO dto) {
        RewardVO vo = rewardService.publish(dto);
        return Result.success(vo, "发布成功");
    }

    @GetMapping("/list")
    public Result list(@RequestParam(required = false) Integer categoryId,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer size) {
        PageVO<RewardVO> vo = rewardService.list(categoryId, page, size);
        return Result.success(vo);
    }

    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        RewardVO vo = rewardService.detail(id);
        return Result.success(vo);
    }

    @GetMapping("/my/list")
    public Result myList(@RequestParam(defaultValue = "1") Integer page,
                         @RequestParam(defaultValue = "10") Integer size) {
        PageVO<RewardVO> vo = rewardService.myList(page, size);
        return Result.success(vo);
    }

    @PostMapping("/accept/{id}")
    public Result accept(@PathVariable Long id) {
        rewardService.accept(id);
        return Result.success("接单成功");
    }

    @PostMapping("/complete/{id}")
    public Result complete(@PathVariable Long id) {
        rewardService.complete(id);
        return Result.success("已确认完成");
    }

    @PostMapping("/cancel/{id}")
    public Result cancel(@PathVariable Long id) {
        rewardService.cancel(id);
        return Result.success("已取消");
    }
}
