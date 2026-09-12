package org.example.common.vo.UserAllVO;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String avatar;
    private Integer gender;
    private String school;
    private String studentNo;
    private Integer creditScore;
    private Integer creditLevel;
    private Integer status;
}
