package org.example.orderservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.example.common.Result;
import org.example.common.dto.AfterSaleApplyDTO;
import org.example.common.dto.OrdinaryOrderDTO;
import org.example.common.utils.UserHolder;
import org.example.common.vo.*;
import org.example.common.vo.OrderDetailVO.DetailOrderVO;
import org.example.orderservice.Service.AfterSaleService;
import org.example.orderservice.Service.OrderService;
import org.springframework.data.annotation.Reference;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Resource
    private OrderService orderService;
    @Resource
    private AfterSaleService afterSaleService;


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
    public Result getList(@RequestParam @NotNull Integer role, @RequestParam(required = false) Integer status,@RequestParam(defaultValue = "10") Integer size,@RequestParam(defaultValue = "1") Integer page){
        Long id = UserHolder.getUser().getId();
        PageVO<OrderListVO> vo=orderService.myOrderList(role,status,page,size,id);
        return Result.success(vo);
    }
    @PostMapping("/cancel/{id}")
    public Result cancel( @PathVariable Long id ,@RequestParam String reason){
        orderService.cancel(id,reason);
        return Result.success("订单已取消");
    }
    @PostMapping("/pay/{id}")
    public Result pay(@PathVariable Long id,@RequestParam Integer payType){
        orderService.pay(id,payType);
        return Result.success("支付成功");
    }
    @PostMapping("/deliver/{id}")
    public Result deliver(@PathVariable Long id){
            orderService.deliver(id);
            return Result.success("卖家已发货");
    }
    @PostMapping("receive/{id}")
    public Result receive(@PathVariable Long id){
        orderService.receive(id);
        return Result.success("买家确认收货，订单完成");
    }
    @PostMapping("/aftersale/apply")
    public Result afterSale(@RequestBody AfterSaleApplyDTO dto){
        AfterSaleVO afterSaleVO = afterSaleService.aftersale(dto);
        return Result.success(afterSaleVO);
    }

    @GetMapping("/aftersale/list")
    public Result aftersaleList(@RequestParam @NotNull Integer role,
                                @RequestParam(required = false) Integer status,
                                @RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size) {
        Long id = UserHolder.getUser().getId();
        PageVO<AfterSaleVO> vo = afterSaleService.myAfterSaleList(role, status, page, size, id);
        return Result.success(vo);
    }

    @GetMapping("/aftersale/{id}")
    public Result aftersaleDetail(@PathVariable Long id) {
        AfterSaleVO vo = afterSaleService.getDetail(id);
        return Result.success(vo);
    }

    @PostMapping("/aftersale/cancel/{id}")
    public Result cancelAfterSale(@PathVariable Long id) {
        afterSaleService.cancel(id);
        return Result.success("售后申请已撤销");
    }



}
