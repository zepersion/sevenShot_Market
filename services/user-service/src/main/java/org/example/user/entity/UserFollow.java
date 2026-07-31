package org.example.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_follow")
public class UserFollow {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 关注者id */
    private Long userId;
    /** 被关注者id */
    private Long followUserId;
    /** 创建时间 */
    private LocalDateTime createTime;
}
