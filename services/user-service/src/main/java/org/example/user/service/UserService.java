package org.example.user.service;

import jakarta.servlet.http.HttpSession;
import org.example.common.DTO.AllUserDTO.CodeDTO;
import org.example.common.DTO.AllUserDTO.RegisterDTO;
import org.example.common.DTO.AllUserDTO.UserLoginDto;
import org.example.common.DTO.AllUserDTO.UserUpdateDTO;
import org.example.common.VO.UserAllVO.RegisterVO;
import org.example.common.VO.UserAllVO.UserLoginVo;
import org.example.user.entity.User;


public interface UserService {


    void sendCode(CodeDTO codeDto);

    RegisterVO register(RegisterDTO registerDTO);

    UserLoginVo login(UserLoginDto userLoginDto, HttpSession session);

    void updateInfo(Long userId, UserUpdateDTO dto);

    User getById(Long id);
}
