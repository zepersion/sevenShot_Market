package org.example.common.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointsVO {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 类型：1签到 2完成悬赏 3发布悬赏 4兑换 5系统奖励
     */
    private Integer type;

    /**
     * 类型文字描述
     */
    private String typeDesc;

    /**
     * 积分变动值（正数增加，负数扣减）
     */
    private Integer changeValue;

    /**
     * 变动前积分
     */
    private Integer beforeValue;

    /**
     * 变动后积分
     */
    private Integer afterValue;

    /**
     * 关联业务id（悬赏id/兑换订单id）
     */
    private Long relatedId;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
