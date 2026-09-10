package org.example.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.common.DTO.AllUserDTO.SignInDTO;
import org.example.common.utils.UserHolder;
import org.example.common.VO.UserAllVO.SignInVO;
import org.example.user.entity.UserSign;
import org.example.user.mapper.UserSignMapper;
import org.example.user.service.UserSignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.example.common.RedisConstants.USER_CONTINUESIGN_KEY;
import static org.example.common.RedisConstants.USER_SIGN_KEY;

@Slf4j
@Service
public class UserSignServiceImpl extends ServiceImpl<UserSignMapper, UserSign> implements UserSignService {

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
                .orderByDesc("continuous_Days")
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

    @Override
    public SignInVO signRecord(SignInDTO dto) {
        SignInVO vo = new SignInVO();
        //获取当前用户
        Long id = UserHolder.getUser().getId();
        //获取当前日期
        LocalDateTime now = LocalDateTime.now();
        //拼接key
        String keyFix= now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String key=USER_SIGN_KEY+":"+id+keyFix ;
        //获取今天是第几天
        int dayOfMonth = now.getDayOfMonth()-1;
//查询今天是否签到
        Boolean isSign = stringRedisTemplate.opsForValue().getBit(key, dayOfMonth - 1);
        if(Boolean.TRUE.equals(isSign)){
           vo.setTodaySigned(true);
        }else
            vo.setTodaySigned(false);
        //continusday的key
        String continueDayKey=USER_CONTINUESIGN_KEY+id;
        String s = stringRedisTemplate.opsForValue().get(continueDayKey);
        vo.setContinuousDays(Integer.parseInt(s));
        //统计签到的天数
            //使用redis中的bitfiled
        List<Long>  totalSignDays_list = stringRedisTemplate.opsForValue().bitField(key, BitFieldSubCommands.create().get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(1));
            if(totalSignDays_list.isEmpty() || totalSignDays_list==null){
                vo.setTotalSignDays(0);
            }
            int totalSignDays =0;
            Long num=totalSignDays_list.get(0);
            while(true){
                if((num & 1)==0){
                    break;
                }else {
                    totalSignDays++;
                }num>>>=1;
            }
            vo.setTotalSignDays(totalSignDays);
            //哪些日期签到了
        ArrayList<String> signedDates = new ArrayList<>();
        int offest_day;
            for(offest_day=1;offest_day<=31;offest_day++){
                Boolean bit = stringRedisTemplate.opsForValue().getBit(key, offest_day);
                if(Boolean.TRUE.equals(bit)){
                    signedDates.add(String.format( "%02d",offest_day));
                }
            }
            vo.setSignedDates(signedDates);
            return vo;
    }
}
