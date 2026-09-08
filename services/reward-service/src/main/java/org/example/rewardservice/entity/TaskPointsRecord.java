package org.example.rewardservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("points_record")
public class TaskPointsRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Long userId;
    private Integer type;
    private Integer change_value;
    private Integer before_value;
    private Integer after_value;
    private Long related_id;
    private String remark;
    private LocalDateTime createTime;
}
