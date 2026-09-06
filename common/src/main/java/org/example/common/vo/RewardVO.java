package org.example.common.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RewardVO {

    private Long taskId;

    private Long taskPublisherId;

    private String taskTitle;

    private String taskDescription;

    private BigDecimal taskAmount;

    private Integer categoryId;

    private String taskCover;

    private List<String> taskImages;

    private LocalDateTime taskDeadline;

    private String taskAddress;

    private String taskContact;

    private Integer taskStatus;

    private String taskStatusName;

    private Long taskReceiverId;

    private LocalDateTime taskAcceptTime;

    private LocalDateTime taskCompleteTime;

    private Integer taskViews;

    private LocalDateTime taskCreateTime;
}
