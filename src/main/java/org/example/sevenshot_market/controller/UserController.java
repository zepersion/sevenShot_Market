package org.example.sevenshot_market.controller;

import jakarta.validation.Valid;

import org.example.sevenshot_market.common.Result;
import org.example.sevenshot_market.dto.CodeDTO;
import org.example.sevenshot_market.dto.RegisterDTO;
import org.example.sevenshot_market.service.UserService;
import org.example.sevenshot_market.vo.RegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/code")
    public Result sendCode(@Valid @RequestBody CodeDTO codeDto) {
        userService.sendCode(codeDto);
        return Result.success(null,"验证码已发送");
    }
    @PostMapping("/register")
    public  Result register(@RequestBody RegisterDTO registerDTO) {
       RegisterVO vo= userService.register(registerDTO);
        return Result.success(vo,"注册成功");
    }
}
