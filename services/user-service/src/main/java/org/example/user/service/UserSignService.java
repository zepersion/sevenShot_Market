package org.example.user.service;

import org.example.common.dto.SignInDTO;
import org.example.common.vo.SignInVO;
import org.example.common.vo.UserVO;

public interface UserSignService {
    SignInVO sign(SignInDTO dto);

    SignInVO signRecord(SignInDTO dto);
}
