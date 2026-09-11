package org.example.rewardservice.FeignClient;

import org.example.common.Result;
import org.example.common.VO.GoodsAllVO.GoodsVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "goods-service", path = "/api/goods")
public interface GoodsFeignClient {
    @GetMapping("/api/goods/listValidGoods")
    Result<List<GoodsVO>> listValidGoods();

    @PostMapping("/api/goods/listByIds")
    Result<List<GoodsVO>> listByIds(@RequestBody List<Long> ids);
}
