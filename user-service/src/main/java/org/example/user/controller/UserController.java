package org.example.user.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.example.common.Result;
import org.example.common.dto.*;
import org.example.user.service.UserService;
import org.example.common.utils.UserHolder;
import org.example.common.vo.RegisterVO;
import org.example.common.vo.SignInVO;
import org.example.common.vo.UserLoginVo;
import org.example.common.vo.UserVO;
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
        return Result.success(null, "验证码已发送");
    }

    @PostMapping("/register")
    public Result register(@RequestBody RegisterDTO registerDTO, HttpSession session) {
        RegisterVO vo = userService.register(registerDTO);

        UserDto loginUser = new UserDto();
        loginUser.setId(vo.getUserId());
        loginUser.setUsername(registerDTO.getUsername());
        loginUser.setPhone(registerDTO.getPhone());
        loginUser.setStudentNo(registerDTO.getStudentNo());
        session.setAttribute("loginUser", loginUser);

        return Result.success(vo, "注册成功");
    }

    @PostMapping("/login")
    public Result login(@RequestBody UserLoginDto userLoginDto, HttpSession session) {
        UserLoginVo vo = userService.login(userLoginDto, session);
        return Result.success(vo, "登录成功");
    }

    @GetMapping("/info")
    public Result currentUser() {
        UserDto user = UserHolder.getUser();
        return Result.success(user);
    }
    @PutMapping("/info")
    public Result updateUser(@RequestBody UserUpdateDTO dto) {

            UserDto user = UserHolder.getUser();
        userService.updateInfo(user.getId(), dto);
        return Result.success();
    }
    @PostMapping("/sign")
    public Result sign() {
        SignInVO vo=userService.sign();
        return Result.success(vo, "<UNK>");
    }
}
