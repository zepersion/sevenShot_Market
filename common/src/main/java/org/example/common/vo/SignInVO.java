package org.example.common.vo;

import lombok.Data;

@Data
public class SignInVO {
    private String date;             // 签到日期 2026-07-26
    private Integer consecutiveDays; // 连续签到天数
    private Integer reward;          // 本次奖励积分
    private Integer totalCredit;     // 签到后总积分
}
