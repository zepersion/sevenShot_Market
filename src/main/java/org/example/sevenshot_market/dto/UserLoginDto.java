package org.example.sevenshot_market.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class UserLoginDto {



@Size(min = 3,max = 50, message = "用户名在3~50个字符之间")
private String nickname;

@NotBlank(message = "密码不能为空")
@Size(min = 3,max=32,message = "密码在3~32位数之间")
private String password;



@Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
private String phone;
/*符号	含义
\d	数字 0-9
[3-9]	数字 3~9
{n}	匹配 n 位
^	字符串开头
$	字符串结尾
[a-zA-Z0-9]	大小写字母 + 数字
.	任意字符，要匹配点需转义 \\.*/





}
