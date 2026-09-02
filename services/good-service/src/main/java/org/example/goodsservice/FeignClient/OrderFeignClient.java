package org.example.goodsservice.FeignClient;

import org.example.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service",path = "/api/order")
public interface OrderFeignClient {
    @GetMapping("/{orderNo}")
    public Result getOrderByOrderNo(@PathVariable Long orderNo);
}
