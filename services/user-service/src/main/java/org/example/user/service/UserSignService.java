package org.example.user.service;

import org.example.common.DTO.AllUserDTO.SignInDTO;
import org.example.common.VO.UserAllVO.SignInVO;

public interface UserSignService {
    SignInVO sign(SignInDTO dto);

    SignInVO signRecord(SignInDTO dto);
}
