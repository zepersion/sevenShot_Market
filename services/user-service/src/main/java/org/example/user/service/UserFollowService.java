package org.example.user.service;

import FollowUserVO;
import org.example.common.VO.PageVO;
import org.example.user.entity.UserFollow;

public interface UserFollowService {
    FollowUserVO isFollow(Long userId);

    PageVO<UserFollow> getFollowList(Integer page, Integer type,Long userId, Integer size);

}
