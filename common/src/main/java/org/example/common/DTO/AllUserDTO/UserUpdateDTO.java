package org.example.common.DTO.AllUserDTO;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String nickname;
    private String avatar;
    private Integer gender;
    private String school;
}
