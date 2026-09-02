package org.example.orderservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.orderservice.entity.OrderStatusLog;

@Mapper
public interface OrderStatusLogMapper extends BaseMapper<OrderStatusLog> {
}
