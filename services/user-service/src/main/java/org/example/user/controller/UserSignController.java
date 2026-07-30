package org.example.user.controller;

import org.example.common.Result;
import org.example.common.dto.SignInDTO;
import org.example.common.vo.SignInVO;
import org.example.common.vo.UserVO;
import org.example.user.service.UserSignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserSignController {
    @Autowired
    private UserSignService userSignService;
    @PostMapping
    public Result<SignInVO> userSign(@RequestBody SignInDTO dto) {
        SignInVO vo=userSignService.sign(dto);
        return Result.success(vo,"签到成功");
    }
}
