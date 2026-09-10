package org.example.goodsservice.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "Ai-service",path = "/api/ai")
public interface AiFeignClient {
    public Boolean reviewPic(String title,String  description);
}
