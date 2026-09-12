package org.example.common.dto.AllRewardDTO;

import lombok.Data;

@Data
public class PointsDTO {
    /**
     * 页码
     */
    private Long pageNum;

    /**
     * 每页条数
     */
    private Long pageSize;

    /**
     * 业务类型：1签到 2完成悬赏 3发布悬赏 4兑换 5系统奖励
     */
    private Integer type;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    private Integer page;
    private Integer size;
}
