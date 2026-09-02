package org.example.orderservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.example.common.Result;
import org.example.common.dto.OrdinaryOrderDTO;
import org.example.common.utils.UserHolder;
import org.example.common.vo.OrderDetailVO.DetailOrderVO;
import org.example.common.vo.OrderListVO;
import org.example.common.vo.OrderResultVO;
import org.example.common.vo.OrdinaryOrderVO;
import org.example.common.vo.PageVO;
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
    @GetMapping("/result/{orderNo}")
    public  Result getOrderByOrderNo(@PathVariable Long orderNo){
        OrderResultVO vo=orderService.getOrderByOrderNo(orderNo);
        return Result.success(vo);
    }
    //GET /api/order/{orderNo}
    @GetMapping("/{id}")
    public  Result getOrderByid(@PathVariable Long id) throws JsonProcessingException {
        DetailOrderVO vo=orderService.getOrderByid(id);
        return Result.success(vo);
    }
    @GetMapping("/list")
    public Result getList(@RequestParam @NotNull Integer role, @RequestParam Integer status,@RequestParam Integer size,@RequestParam Integer page){
        Long id = UserHolder.getUser().getId();
        PageVO<OrderListVO> vo=orderService.myOrderList(role,status,page,size,id);
        return Result.success(vo);
    }
    @PostMapping("/cancel/{id}")
    public Result cancel( @PathVariable Long id ,@RequestParam String reason){
        orderService.cancel(id,reason);
        return Result.success("订单已删除");
    }
}
