package org.example.common.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RewardVO {

    private Long id;

    private Long publisherId;

    private String title;

    private String description;

    private BigDecimal rewardAmount;

    private Integer categoryId;

    private String coverImage;

    private List<String> images;

    private LocalDateTime deadline;

    private Integer status;

    private String statusName;

    private Long acceptorId;

    private LocalDateTime acceptTime;

    private LocalDateTime completeTime;

    private Integer viewCount;

    private LocalDateTime createTime;
}
