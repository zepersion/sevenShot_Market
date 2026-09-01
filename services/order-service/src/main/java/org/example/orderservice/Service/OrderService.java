package org.example.orderservice.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.common.dto.OrdinaryOrderDTO;
import org.example.common.vo.OrderResultVO;
import org.example.common.vo.OrdinaryOrderVO;
import org.example.orderservice.entity.Order;

public interface OrderService extends IService<Order> {

    OrdinaryOrderVO createOrder(OrdinaryOrderDTO dto);

    OrderResultVO getOrderByid(Long orderNo);
}
