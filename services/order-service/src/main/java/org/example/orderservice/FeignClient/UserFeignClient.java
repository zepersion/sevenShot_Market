package org.example.orderservice.FeignClient;

import org.example.common.Result;
import org.example.common.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/api/user")
public interface UserFeignClient {
    @GetMapping("/{id}")
    Result<UserDto> getUserById(@PathVariable Long id);
}