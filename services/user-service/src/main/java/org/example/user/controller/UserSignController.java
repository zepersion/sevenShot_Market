package org.example.user.controller;

import org.example.common.Result;
import org.example.common.dto.SignInDTO;
import org.example.common.vo.SignInVO;
import org.example.common.vo.UserVO;
import org.example.user.service.UserSignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/sign")
public class UserSignController {
    @Autowired
    private UserSignService userSignService;
    @PostMapping
    public Result<SignInVO> userSign(@RequestBody SignInDTO dto) {
        SignInVO vo=userSignService.sign(dto);
        return Result.success(vo,"签到成功");
    }
    @GetMapping("/record")
    public  Result<SignInVO> userSignRecord(SignInDTO dto) {
        SignInVO signInVO = userSignService.signRecord(dto);
        return Result.success(signInVO,"success");

    }
}
