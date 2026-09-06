package org.example.rewardservice.Task;

import jakarta.annotation.Resource;
import org.example.rewardservice.entity.Reward;
import org.example.rewardservice.service.RewardService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Set;

import static org.example.common.RedisConstants.REWARD_VIEW_KEY;

public class DetailTask {
    @Resource
    private RewardService rewardService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Scheduled(cron = "0 */10 * * * ?")
    public void detailTask() {
    String viewKey=REWARD_VIEW_KEY+"*";
        Set<String> keys = stringRedisTemplate.keys(viewKey);
        for (String key : keys) {
            //取出除去key 找出我们的Rewardid
            String idStr=key.replace(REWARD_VIEW_KEY, "");
            //转化成long类型
            Long rewardId=Long.parseLong(idStr);
            Integer count = Integer.valueOf(stringRedisTemplate.opsForValue().get(key));
            Reward t = new Reward();
            t.setId(rewardId);
            t.setViewCount(count);
            rewardService.updateById(t);
        }


    }
}
