package org.example.orderservice.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.NotNull;
import org.example.common.dto.AfterSaleApplyDTO;
import org.example.common.dto.OrdinaryOrderDTO;
import org.example.common.vo.*;
import org.example.common.vo.OrderDetailVO.DetailOrderVO;
import org.example.orderservice.entity.Order;

public interface OrderService extends IService<Order> {

    OrdinaryOrderVO createOrder(OrdinaryOrderDTO dto);

    OrderResultVO getOrderByOrderNo(Long orderNo);

    DetailOrderVO getOrderByid(Long id) throws JsonProcessingException;

    PageVO<OrderListVO> myOrderList(@NotNull Integer role, Integer status, Integer page, Integer size,Long id );

    void cancel(Long id, String reason);

    void pay(Long id, Integer payType);

    void deliver(Long id);

    void receive(Long id);
}
