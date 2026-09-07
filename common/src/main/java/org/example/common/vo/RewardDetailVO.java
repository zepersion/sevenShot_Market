package org.example.common.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RewardDetailVO {

    private Long id;

    private Long publisherId;

    private String publisherName;

    private Long acceptorId;

    private String acceptorName;

    private Long categoryId;

    private String categoryName;

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
