package org.example.goodsservice.Task;

import jakarta.annotation.Resource;
import org.example.goodsservice.service.GoodsService;
import org.springframework.scheduling.annotation.Scheduled;

public class SeckillPreloadTask {
    @Resource
    private GoodsService goodsService;

    @Scheduled(fixedDelay = 300000)
    public void seckillPreload() {
        goodsService.SeckillPreload();
        System.out.println("秒杀预热已完成");
    }
}
