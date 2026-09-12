package org.example.common.vo.UserAllVO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowUserVO {
    private Long userId;
    private String nickName;
    private String icon;
    /** 是否互相关注 */
    private Boolean isMutual;
    /** 当前登录人是否关注该用户 */
    private Boolean isFollow;
    private LocalDateTime followTime;
}
