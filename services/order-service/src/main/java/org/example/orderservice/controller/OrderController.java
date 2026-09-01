package org.example.orderservice.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.common.Result;
import org.example.common.dto.OrdinaryOrderDTO;
import org.example.common.vo.OrderResultVO;
import org.example.common.vo.OrdinaryOrderVO;
import org.example.orderservice.Service.OrderService;
import org.springframework.data.annotation.Reference;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Resource
    private OrderService orderService;



    @PostMapping("/create")
    public Result<OrdinaryOrderVO> createOrder(@RequestBody @Valid OrdinaryOrderDTO dto) {
        OrdinaryOrderVO vo = orderService.createOrder(dto);
        return Result.success(vo, "下单成功");
    }
    //GET /api/order/{orderNo}
    @GetMapping("/{orderNo}")
    public  Result getOrderByid(@PathVariable Long orderNo){
        OrderResultVO vo=orderService.getOrderByid(orderNo);
        return Result.success(vo);

    }
}
