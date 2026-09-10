package org.example.goodsservice.FeignClient;

import org.example.common.Result;
import org.example.common.DTO.AllUserDTO.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/api/user")
public interface UserFeignClient {
    @GetMapping("/{id}")
    Result<UserDTO> getUserById(@PathVariable Long id);
}
