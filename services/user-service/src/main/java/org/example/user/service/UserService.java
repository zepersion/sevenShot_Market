package org.example.user.service;

import jakarta.servlet.http.HttpSession;
import org.example.common.dto.AllUserDTO.CodeDTO;
import org.example.common.dto.AllUserDTO.RegisterDTO;
import org.example.common.dto.AllUserDTO.UserLoginDto;
import org.example.common.dto.AllUserDTO.UserUpdateDTO;
import org.example.common.vo.UserAllVO.RegisterVO;
import org.example.common.vo.UserAllVO.UserLoginVo;
import org.example.user.entity.User;


public interface UserService {


    void sendCode(CodeDTO codeDto);

    RegisterVO register(RegisterDTO registerDTO);

    UserLoginVo login(UserLoginDto userLoginDto, HttpSession session);

    void updateInfo(Long userId, UserUpdateDTO dto);

    User getById(Long id);
}
