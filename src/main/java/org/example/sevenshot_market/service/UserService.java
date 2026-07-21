package org.example.sevenshot_market.service;

import jakarta.servlet.http.HttpSession;
import org.example.sevenshot_market.dto.CodeDTO;
import org.example.sevenshot_market.dto.RegisterDTO;
import org.example.sevenshot_market.dto.UserDto;
import org.example.sevenshot_market.vo.RegisterVO;


public interface UserService {


    void sendCode(CodeDTO codeDto);

    RegisterVO register(RegisterDTO registerDTO);
}
