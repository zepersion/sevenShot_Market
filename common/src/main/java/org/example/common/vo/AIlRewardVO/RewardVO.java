package org.example.common.vo.AIlRewardVO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RewardVO {

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

    private String statusName;

    private String tags;

    private Integer viewCount;

    private Integer hotScore;

    private LocalDateTime createTime;

    private LocalDateTime acceptTime;

    private LocalDateTime finishTime;

    private LocalDateTime updateTime;
}
