package org.example.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@TableName("credit_record")
public class UserCredit {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 变动类型：1交易成功 2好评 3违约 4违规 5售后纠纷
     */
    private Integer changeType;

    /**
     * 变动值（正数加分、负数扣分）
     */
    private Integer changeValue;

    /**
     * 变动前信用分
     */
    private Integer beforeScore;

    /**
     * 变动后信用分
     */
    private Integer afterScore;

    /**
     * 变动原因
     */
    private String reason;

    /**
     * 关联业务ID（订单ID/违规ID）
     */
    private Long relatedId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
