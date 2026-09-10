package org.example.common.DTO.AllUserDTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
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
    private LocalDateTime createTime;
    private String code;
}
