package org.example.rewardservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_points")
public class UserPoints {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer totalPoints;//总积分
    private Integer availablePoints;//可用积分
    private Integer frozenPoints;//冻结积分
    private LocalDateTime updateTime;
}
