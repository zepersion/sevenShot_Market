package org.example.user.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.common.vo.PageVO;
import org.example.user.entity.User;
import org.example.user.entity.UserCredit;
import org.example.user.mapper.UserCreditMapper;
import org.example.user.mapper.UserMapper;
import org.example.user.service.UserCreditService;
import org.example.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserCreditServiceImpl extends ServiceImpl<UserCreditMapper,UserCredit> implements UserCreditService {
@Autowired
  private   UserCreditMapper userCreditMapper;
@Autowired
private UserMapper userMapper;

    @Override
    public PageVO<UserCredit> getCreditRecords(Integer page, Integer size, Long id) {
       //查询需要我们的数据,先创建我们mybatis-plus里面存自带的分页表对象
        Page<UserCredit> creditParam = new Page<>(page, size);
        //查询我们的数据
        Page<UserCredit> pageResult = lambdaQuery().eq(UserCredit::getUserId, id).orderByAsc(UserCredit::getCreateTime).page(creditParam);
        PageVO<UserCredit> vo = new PageVO<>();
        vo.setRecords(pageResult.getRecords());
        vo.setPages(pageResult.getPages());
        vo.setTotal(pageResult.getTotal());
        vo.setCurrent(pageResult.getCurrent());
        vo.setSize(pageResult.getSize());
        return vo;
    }

    @Override
    @Transactional
    public void updateCredit(Long id, Integer score) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        int beforeScore = user.getCreditScore() == null ? 0 : user.getCreditScore();
        int afterScore = Math.max(beforeScore + score, 0);

        // 更新用户信用分和等级
        User updateUser = new User();
        updateUser.setId(id);
        updateUser.setCreditScore(afterScore);
        updateUser.setCreditLevel(calculateLevel(afterScore));
        updateUser.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(updateUser);

        // 插入信用变动记录
        UserCredit record = new UserCredit();
        record.setUserId(id);
        record.setChangeType(1); // 1=交易成功
        record.setChangeValue(score);
        record.setBeforeScore(beforeScore);
        record.setAfterScore(afterScore);
        record.setReason("交易完成，信用分" + (score >= 0 ? "+" : "") + score);
        record.setCreateTime(LocalDateTime.now());
        userCreditMapper.insert(record);
    }

    private int calculateLevel(int score) {
        if (score >= 90) return 1; // 优秀
        if (score >= 80) return 2; // 良好
        if (score >= 60) return 3; // 普通
        if (score >= 40) return 4; // 较差
        return 5;                    // 极差
    }
}
