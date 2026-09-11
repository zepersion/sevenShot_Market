package org.example.goodsservice.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "Ai-service",path = "/api/ai")
public interface AiFeignClient {
    @PostMapping("/reviewPic")
    public Boolean reviewPic(String title,String  description);
}
