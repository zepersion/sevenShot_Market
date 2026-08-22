package org.example.goodsservice.controller;

import jakarta.annotation.Resource;
import org.example.common.Result;
import org.example.common.dto.SeckillSaleDTO;
import org.example.common.vo.SeckillGoodsDataVO;
import org.example.common.vo.SeckillSaleVO;
import org.example.goodsservice.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seckill")
public class SeckillController {
    @Resource
    private GoodsService goodsService;

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
}
