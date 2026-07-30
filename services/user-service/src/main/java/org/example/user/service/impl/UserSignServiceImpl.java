package org.example.user.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.SignInDTO;
import org.example.common.utils.UserHolder;
import org.example.common.vo.SignInVO;
import org.example.common.vo.UserVO;
import org.example.user.entity.User;
import org.example.user.entity.UserSign;
import org.example.user.mapper.UserSignMapper;
import org.example.user.service.UserService;
import org.example.user.service.UserSignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.example.common.RedisConstants.USER_CONTINUESIGN_KEY;
import static org.example.common.RedisConstants.USER_SIGN_KEY;

@Slf4j
@Service
public class UserSignServiceImpl extends ServiceImpl<UserSignMapper, UserSign> implements UserSignService {
@Autowired
private UserService userService;
@Autowired
private StringRedisTemplate stringRedisTemplate;
    @Override
    public SignInVO sign(SignInDTO dto) {
        //获取当前用户
        Long id = UserHolder.getUser().getId();
        //获取当前日期
        LocalDateTime now = LocalDateTime.now();
        //拼接key
        String keyFix= now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key=USER_SIGN_KEY+keyFix+id ;
        //continusday的key
        String continueDayKey=USER_CONTINUESIGN_KEY+id;
        //获取今天是第几天
        int dayOfMonth = now.getDayOfMonth()-1;

//查询今天是否签到
        Boolean isSign = stringRedisTemplate.opsForValue().getBit(key, dayOfMonth - 1);
        if(Boolean.TRUE.equals(isSign)){
            log.error("今日已经签到,明天再来吧");

        }
        //查询数据库上次有没有签到
        UserSign lastSign = query().eq("user_id", id)
                .orderByDesc("continuousDays")
                .last("limit 1").one();
        //计算连续登陆天数
        int continuousDays = 1;
        if(lastSign != null){
            LocalDate signDate = lastSign.getSignDate();
                    //判断是不是昨天
                if (signDate.equals(now.minusDays(1))) {
                    //是
                    continuousDays=lastSign.getContinuousDays()+1;
                }

        }
        
        
        //积分添加
        int rewardPoints;
        if (continuousDays <= 3) {
            rewardPoints = 1;
        } else if (continuousDays <= 7) {
            rewardPoints = 3;
        } else {
            rewardPoints = 5;
        }

        UserSign user = new UserSign();
        user.setUserId(id);
        user.setSignDate(LocalDate.from(now));
        user.setContinuousDays(continuousDays);
        user.setPointsReward(rewardPoints);
        this.save(user);
        //签到
        stringRedisTemplate.opsForValue().setBit(key,dayOfMonth,true);
        stringRedisTemplate.opsForValue().set(continueDayKey,String.valueOf(continuousDays));
        //返回vo
        SignInVO vo = new SignInVO();
        vo.setContinuousDays(continuousDays);
        vo.setTodaySigned(true);
        vo.setPointsReward( rewardPoints);
        return vo;
    }
}
