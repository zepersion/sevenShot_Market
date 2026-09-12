package org.example.common.dto.AllUserDTO;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String nickname;
    private String avatar;
    private Integer gender;
    private String school;
}
