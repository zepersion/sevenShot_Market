package org.example.user.controller;

import cn.hutool.core.bean.BeanUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.example.common.DTO.AllUserDTO.*;
import org.example.common.Result;
import org.example.common.VO.*;
import org.example.common.VO.UserAllVO.SignInVO;
import org.example.common.VO.UserAllVO.UserLoginVo;
import org.example.user.entity.User;
import org.example.user.entity.UserCredit;
import org.example.user.entity.UserFollow;
import org.example.user.service.UserCreditService;
import org.example.user.service.UserFollowService;
import org.example.user.service.UserService;
import org.example.common.utils.UserHolder;
import org.example.user.service.UserSignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
@FeignClient(name = "user-service", path = "/api/user")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserSignService userSignService;
    @Autowired
    private UserFollowService userFollowService;
    @Autowired
    private UserCreditService userCreditService;

    @PostMapping("/code")
    public Result sendCode(@Valid @RequestBody CodeDTO codeDto) {
        userService.sendCode(codeDto);
        return Result.success(null, "验证码已发送");
    }

    @PostMapping("/register")
    public Result register(@RequestBody RegisterDTO registerDTO, HttpSession session) {
        RegisterVO vo = userService.register(registerDTO);

        UserDTO loginUser = new UserDTO();
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
        UserDTO user = UserHolder.getUser();
        return Result.success(user);
    }

    @PutMapping("/info")
    public Result updateUser(@RequestBody UserUpdateDTO dto) {
        UserDTO user = UserHolder.getUser();
        userService.updateInfo(user.getId(), dto);
        return Result.success();
    }

    @PostMapping("/sign")
    public Result<SignInVO> userSign(@RequestBody SignInDTO dto) {
        SignInVO vo = userSignService.sign(dto);
        return Result.success(vo, "签到成功");
    }

    @GetMapping("/sign/record")
    public Result<SignInVO> userSignRecord(SignInDTO dto) {
        SignInVO signInVO = userSignService.signRecord(dto);
        return Result.success(signInVO, "success");

    }

    @PostMapping("/follow/{userId}")//userId代表被关注的人
    public Result follow(@PathVariable("UserId") Long userId) {
        FollowUserVO vo = userFollowService.isFollow(userId);
        return Result.success(vo, "success");
    }
    @GetMapping("/follow/list")
    public  Result<PageVO<UserFollow>> followList(@RequestParam Integer type,
                              @RequestParam Integer page,
                                                  @RequestParam Integer size) {
        Long userId = UserHolder.getUser().getId();
        PageVO<UserFollow> vo = userFollowService.getFollowList(page,type, userId,size);
            return Result.success(vo, "success");
    }
    @GetMapping("/credit/records")
    public Result getCreditRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size){
        Long id = UserHolder.getUser().getId();
        PageVO<UserCredit> vo=userCreditService.getCreditRecords(page,size,id);
        return Result.success("vo","success");
    }
    @GetMapping("/credit/{id}/{score}")
    public Result updateCredit(@PathVariable Long id, @PathVariable Integer score) {
        userCreditService.updateCredit(id, score);
        return Result.success();
    }
    @GetMapping("/{id}")
    public Result<UserDTO> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) return Result.error("用户不存在");
        UserDTO dto = BeanUtil.copyProperties(user, UserDTO.class);
        return Result.success(dto);
    }
}