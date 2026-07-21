package org.example.sevenshot_market.vo;

import lombok.Data;


@Data
public class UserLoginVo {
    private Long id;              // 用户ID

    private String username;      // 用户名
    private String nickname;      // 昵称
    private String phone;         // 手机号
    private String avatar;        // 头像URL
    private Integer status;       // 状态：0禁用 1正常


}
