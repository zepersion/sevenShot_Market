package org.example.user.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.common.vo.PageVO;
import org.example.user.entity.UserCredit;
import org.example.user.mapper.UserCreditMapper;
import org.example.user.mapper.UserMapper;
import org.example.user.service.UserCreditService;
import org.example.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
