package org.example.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.user.entity.User;
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 空着！不需要写任何重写方法
    // 只有自己写自定义SQL的时候才在这里增加方法
}