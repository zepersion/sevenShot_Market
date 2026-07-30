package org.example.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_sign")
public class UserSign {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 签到日期 */
    private LocalDate signDate;

    /** 连续签到天数 */
    private Integer continuousDays;

    /** 获得积分 */
    private Integer pointsReward;

    /** 创建时间 */
    private LocalDateTime createTime;
}
