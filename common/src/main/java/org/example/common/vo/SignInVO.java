package org.example.common.vo;

import lombok.Data;

@Data
public class SignInVO {
    private Integer continuousDays;  // 连续签到天数
    private Integer pointsReward;    // 本次奖励积分
    private Boolean todaySigned;     // 今天是否已签到
}
