package org.example.common.vo.UserAllVO;

import lombok.Data;

@Data
public class UserLoginVo {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String avatar;
    private Integer status;
    private String token;
}
