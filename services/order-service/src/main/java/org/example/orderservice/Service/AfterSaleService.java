package org.example.orderservice.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.common.dto.AllOrderDTO.AfterSaleApplyDTO;
import org.example.common.vo.OrderVO.AfterSaleVO;
import org.example.common.vo.PageVO;
import org.example.orderservice.entity.AfterSale;

public interface AfterSaleService extends IService<AfterSale> {

    AfterSaleVO aftersale(AfterSaleApplyDTO dto);

    PageVO<AfterSaleVO> myAfterSaleList(Integer role, Integer status, Integer page, Integer size, Long userId);

    AfterSaleVO getDetail(Long id);

    void cancel(Long id);
}
