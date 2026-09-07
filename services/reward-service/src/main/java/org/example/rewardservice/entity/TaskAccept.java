package org.example.rewardservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_accept")
public class TaskAccept {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long acceptorId;
    private String message;
    private Integer status;
    private LocalDateTime createTime;

}
