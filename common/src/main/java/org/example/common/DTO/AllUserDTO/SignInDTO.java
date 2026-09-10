package org.example.common.DTO.AllUserDTO;

import lombok.Data;

@Data
public class SignInDTO {
    // 签到不需要传参，用户从 token 获取
    // 如需按年月查询签到记录，放开下面字段
    // private String yearMonth; // 查询月份 2026-07
}
