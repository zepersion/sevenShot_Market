package org.example.rewardservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("reward")
public class Reward {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long publisherId;

    private String title;

    private String description;

    private BigDecimal rewardAmount;

    private Integer categoryId;

    private String coverImage;

    private String images;

    private LocalDateTime deadline;

    private Integer status;

    private Long acceptorId;

    private LocalDateTime acceptTime;

    private LocalDateTime completeTime;

    private Integer viewCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
