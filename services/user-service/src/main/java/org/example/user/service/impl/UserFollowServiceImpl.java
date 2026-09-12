package org.example.user.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.common.vo.UserAllVO.FollowUserVO;
import org.example.common.utils.UserHolder;

import org.example.common.vo.PageVO;
import org.example.user.entity.User;
import org.example.user.entity.UserFollow;
import org.example.user.mapper.UserFollowMapper;
import org.example.user.mapper.UserMapper;
import org.example.user.service.UserFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.example.common.RedisConstants.USER_FANS_KEY;
import static org.example.common.RedisConstants.USER_FOLLOW_KEY;

@Service
@Slf4j
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper,UserFollow> implements UserFollowService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
@Autowired
private UserMapper userMapper;

    //这个比起之前传true和false更安全
    @Transactional(rollbackFor = Exception.class)
    @Override
    public FollowUserVO isFollow(Long userId) {
        Long id = UserHolder.getUser().getId();
        if(id.equals(userId)) {
            log.error("用户不可以自我关注");
            throw new RuntimeException("用户不可以自我关注");
        }
        UserFollow fowllowUser = getOne(query().eq("user_id", id).eq("follow_user_id", userId), false);
        String userFollowKey  = USER_FOLLOW_KEY + id;
        String followUserKey = USER_FANS_KEY + userId;
        //时间戳
        long timeStamp = System.currentTimeMillis();
        FollowUserVO vo = new FollowUserVO();
        //切换按钮
        if(fowllowUser != null) {
            //已经有记录 取关
            stringRedisTemplate.opsForZSet().remove(userFollowKey,JSONUtil.toJsonStr( fowllowUser.getFollowUserId()));
            stringRedisTemplate.opsForZSet().remove(followUserKey, JSONUtil.toJsonStr(id));
            removeById(fowllowUser.getId());
            vo.setIsFollow(false);
        }else {
            //关注
            stringRedisTemplate.opsForZSet().add(userFollowKey, String.valueOf(fowllowUser.getFollowUserId()),timeStamp);
            stringRedisTemplate.opsForZSet().add(followUserKey, String.valueOf(id),timeStamp);
            UserFollow userFollow = new UserFollow();
           userFollow.setUserId(id);
            userFollow.setFollowUserId(userId);
            userFollow.setCreateTime(LocalDateTime.now());
            save(userFollow);
            vo.setIsFollow(true);
        }
        return vo;
    }

    @Override
    public PageVO<UserFollow> getFollowList(Integer page, Integer type, Long userId, Integer size) {
    String key = "";
        //关注列表和粉丝列表的key
        if(type == 1) {   key = USER_FOLLOW_KEY + userId;}
                else if(type == 2) {   key = USER_FANS_KEY + userId;}
        //统计关注的和粉丝
        if(key.isBlank()){
            throw new RuntimeException("type传值不正确，只能1关注列表/2粉丝列表");
        }
        // 计算下标
        long start = (long) (page - 1) * size;
        long end = start + size - 1;

        // redis查询
        //分页查询当前页数据
        Set<String> userIdStrSet = stringRedisTemplate.opsForZSet().reverseRange(key, start, end);//start开始查,end结束
        //总数
        Long total = stringRedisTemplate.opsForZSet().zCard(key);
        PageVO<UserFollow> vo = new PageVO<>();
        /*已知：
page：页码（从 1 开始）
size：每页条数1. start（起始下标）
start = (page - 1) \times size
end（结束下标）end = start + size - 1*/
//组装分页VO
        vo.setTotal(total);
        vo.setCurrent(Long.valueOf(page));
        vo.setPages((total + size - 1) / size);
//判断查询的集合是否为空
        if(userIdStrSet.isEmpty()) {
            vo.setRecords(Collections.emptyList());
            return vo;
        }
        List<Long> list = userIdStrSet.stream().map(Long::valueOf).toList();//将用户id转化回long
        //数据库查询
        List<User> users = userMapper.selectByIds(list);
        List<UserFollow> Records = users.stream().map(user -> {
                    UserFollow userFollow = new UserFollow();

                    userFollow.setFollowUserId(userId);
            //Double score = stringRedisTemplate.opsForZSet().score(USER_FOLLOW_KEY + userId, String.valueOf(userFollow.getFollowUserId()));
                    return userFollow;
                }
        ).toList();
        if(Records.isEmpty()) {
            vo.setRecords(Collections.emptyList());
        }else {
            vo.setRecords(Records);
        }
        return vo;
    }


}
