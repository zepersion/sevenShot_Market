package org.example.orderservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@TableName("order_status_log")
public class OrderStatusLog {
    @TableId(type= IdType.AUTO)
    private Integer id;

    private Long orderId;

    private Integer before_status;

    private Integer after_status;

    private Integer operator_type;


    private Integer operator_id;

    private String remark;

    private LocalDateTime create_time;
}
