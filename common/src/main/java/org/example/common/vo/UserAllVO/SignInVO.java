package org.example.common.vo.UserAllVO;

import lombok.Data;

import java.util.List;

@Data
public class SignInVO {
    private Integer continuousDays;  // 连续签到天数
    private Integer pointsReward;    // 本次奖励积分
    private Boolean todaySigned;     // 今天是否已签到
    private List<String> signedDates;//签到的日期
    private Integer totalSignDays;//签到的总日期
}
