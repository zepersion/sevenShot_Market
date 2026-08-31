package org.example.orderservice.controller;

import jakarta.validation.Valid;
import org.example.common.Result;
import org.example.common.dto.OrdinaryOrderDTO;
import org.example.common.vo.OrdinaryOrderVO;
import org.example.orderservice.Service.OrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public Result<OrdinaryOrderVO> createOrder(@RequestBody @Valid OrdinaryOrderDTO dto) {
        OrdinaryOrderVO vo = orderService.createOrder(dto);
        return Result.success(vo, "下单成功");
    }
}
