package org.example.rewardservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("task")
public class Reward {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long publisherId;

    private Long acceptorId;

    private Long categoryId;

    private String title;

    private String description;

    private BigDecimal reward;

    private String location;

    private LocalDateTime deadline;

    private Integer status;

    private String tags;

    private Integer viewCount;

    private Integer hotScore;

    private LocalDateTime createTime;

    private LocalDateTime acceptTime;

    private LocalDateTime finishTime;

    private LocalDateTime updateTime;
}
