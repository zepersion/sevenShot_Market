package org.example.orderservice.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.NotNull;
import org.example.common.dto.AllGoodsDTO.OrdinaryOrderDTO;
import org.example.common.vo.OrderVO.OrderDetailVO.DetailOrderVO;
import org.example.common.vo.OrderVO.OrderListVO;
import org.example.common.vo.OrderVO.OrderResultVO;
import org.example.common.vo.OrderVO.OrdinaryOrderVO;
import org.example.common.vo.PageVO;
import org.example.orderservice.entity.Order;

public interface OrderService extends IService<Order> {

    OrdinaryOrderVO createOrder(OrdinaryOrderDTO dto);

    OrderResultVO getOrderByOrderNo(Long orderNo);

    DetailOrderVO getOrderByid(Long id) throws JsonProcessingException;

    PageVO<OrderListVO> myOrderList(@NotNull Integer role, Integer status, Integer page, Integer size, Long id );

    void cancel(Long id, String reason);

    void pay(Long id, Integer payType);

    void deliver(Long id);

    void receive(Long id);
}
