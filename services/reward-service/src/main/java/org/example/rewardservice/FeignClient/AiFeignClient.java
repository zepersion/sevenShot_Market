package org.example.rewardservice.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "Ai-service", path = "/api/ai")
public interface AiFeignClient {
    @GetMapping("/recommend")
    public String recommend(String text);
}
