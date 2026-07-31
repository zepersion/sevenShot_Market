package org.example.aiservice.FeignClient;

import org.example.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "goods-service",path = "/api/goods")
public interface GoodsFeignClient {
    @PutMapping("/api/goods/internal/updateStatus")
    Result<Void> updateGoodsStatus(
            @RequestParam("goodsId") Long goodsId,
            @RequestParam("status") Integer status,
            @RequestParam("auditReason") String auditReason
    );
}
