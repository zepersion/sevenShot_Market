package org.example.user.service;


import org.example.common.vo.PageVO;
import org.example.common.vo.UserAllVO.FollowUserVO;
import org.example.user.entity.UserFollow;

public interface UserFollowService {
    FollowUserVO isFollow(Long userId);

    PageVO<UserFollow> getFollowList(Integer page, Integer type,Long userId, Integer size);

}
