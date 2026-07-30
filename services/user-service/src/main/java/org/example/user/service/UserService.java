package org.example.user.service;

import jakarta.servlet.http.HttpSession;
import org.example.common.dto.CodeDTO;
import org.example.common.dto.RegisterDTO;
import org.example.common.dto.UserDto;
import org.example.common.dto.UserLoginDto;
import org.example.common.vo.RegisterVO;
import org.example.common.dto.UserUpdateDTO;
import org.example.common.vo.SignInVO;
import org.example.common.vo.UserLoginVo;
import org.example.common.vo.UserVO;


public interface UserService {


    void sendCode(CodeDTO codeDto);

    RegisterVO register(RegisterDTO registerDTO);

    UserLoginVo login(UserLoginDto userLoginDto, HttpSession session);

    void updateInfo(Long userId, UserUpdateDTO dto);


}
