package org.example.sevenshot_market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;              // 用户ID

    private String username;      // 用户名
    private String password;      // 密码（BCrypt加密）
    private String nickname;      // 昵称
    private String phone;         // 手机号
    private String avatar;        // 头像URL
    private Integer gender;       // 性别：0未知 1男 2女
    private String school;        // 学校名称
    private String studentNo;     // 学号
    private Integer creditScore;  // 信用分（初始100）
    private Integer creditLevel;  // 信用等级：1优秀 2良好 3普通 4较差 5极差
    private Integer status;       // 状态：0禁用 1正常

    private LocalDateTime createTime;  // 创建时间
    private LocalDateTime updateTime;  // 更新时间
}
