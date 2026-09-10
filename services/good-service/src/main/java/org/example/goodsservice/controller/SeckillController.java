package org.example.goodsservice.controller;

import jakarta.annotation.Resource;
import org.example.common.Result;
import org.example.common.DTO.AllGoodsDTO.SeckillSaleDTO;
import org.example.common.VO.AllSeckillVO.SeckillGoodsDataVO;
import org.example.goodsservice.FeignClient.OrderFeignClient;
import org.example.goodsservice.service.GoodsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seckill")
public class SeckillController {
    @Resource
    private GoodsService goodsService;
    @Resource
    public OrderFeignClient orderFeignClient;
    @GetMapping("/list")
    public Result seckillList(){
        SeckillGoodsDataVO vo= goodsService.seckillList();
        return Result.success(vo);
    }

    @PostMapping("/order/{goodsId}")
    public Result seckillOrder(@PathVariable("goodsId") Long goodsId, SeckillSaleDTO dto){
       goodsService.seckill(goodsId,dto);
        return Result.success("秒杀请求已提交");

    }
    @GetMapping("/result/{orderNo}")
    public Result seckillResult(@PathVariable Long orderNo){
        Result orderByid = orderFeignClient.getOrderByOrderNo(orderNo);
        return orderByid;
    }
}
