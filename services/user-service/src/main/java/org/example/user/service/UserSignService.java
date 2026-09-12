package org.example.user.service;

import org.example.common.dto.AllUserDTO.SignInDTO;
import org.example.common.vo.UserAllVO.SignInVO;

public interface UserSignService {
    SignInVO sign(SignInDTO dto);

    SignInVO signRecord(SignInDTO dto);
}
