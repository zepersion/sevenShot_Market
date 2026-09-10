package org.example.orderservice.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.common.DTO.AllOrderDTO.AfterSaleApplyDTO;
import org.example.common.utils.UserHolder;
import org.example.common.VO.OrderVO.AfterSaleVO;
import org.example.common.VO.PageVO;
import org.example.orderservice.Service.AfterSaleService;
import org.example.orderservice.entity.AfterSale;
import org.example.orderservice.entity.Order;
import org.example.orderservice.mapper.AfterSaleMapper;
import org.example.orderservice.mapper.OrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AfterSaleServiceImpl extends ServiceImpl<AfterSaleMapper, AfterSale> implements AfterSaleService {

    @Resource
    private OrderMapper orderMapper;

    @Override
    @Transactional
    public AfterSaleVO aftersale(AfterSaleApplyDTO dto) {
        Long userId = UserHolder.getUser().getId();

        // 查订单
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("无权申请售后");
        }
        if (order.getStatus() < 2) {
            throw new RuntimeException("订单状态不支持售后");
        }

        // 检查是否已有售后申请
        Long existCount = lambdaQuery()
                .eq(AfterSale::getOrderId, dto.getOrderId())
                .ne(AfterSale::getStatus, 3) // 排除已撤销的
                .count();
        if (existCount > 0) {
            throw new RuntimeException("该订单已存在售后申请");
        }

        // 组装售后单
        AfterSale afterSale = new AfterSale();
        afterSale.setOrderNo(order.getOrderNo());
        afterSale.setOrderId(order.getId());
        afterSale.setBuyerId(userId);
        afterSale.setSellerId(order.getSellerId());
        afterSale.setGoodsId(order.getGoodsId());
        afterSale.setType(dto.getType());
        afterSale.setReason(dto.getReason());
        afterSale.setDescription(dto.getDescription());
        afterSale.setImages(dto.getImages() != null ? String.join(",", dto.getImages()) : null);
        afterSale.setRefundAmount(dto.getRefundAmount());
        afterSale.setStatus(0); // 待处理
        afterSale.setCreateTime(LocalDateTime.now());
        save(afterSale);

        return convertToVO(afterSale);
    }

    @Override
    public PageVO<AfterSaleVO> myAfterSaleList(Integer role, Integer status, Integer page, Integer size, Long userId) {
        Page<AfterSale> pages = new Page<>(page, size);
        LambdaQueryWrapper<AfterSale> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(role == 1, AfterSale::getBuyerId, userId)
                .eq(role == 2, AfterSale::getSellerId, userId)
                .eq(status != null, AfterSale::getStatus, status)
                .orderByDesc(AfterSale::getCreateTime);
        Page<AfterSale> result = page(pages, wrapper);

        List<AfterSaleVO> list = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageVO<AfterSaleVO> pageVo = new PageVO<>();
        pageVo.setRecords(list);
        pageVo.setTotal(result.getTotal());
        pageVo.setSize(result.getSize());
        pageVo.setCurrent(result.getCurrent());
        return pageVo;
    }

    @Override
    public AfterSaleVO getDetail(Long id) {
        AfterSale afterSale = getById(id);
        if (afterSale == null) {
            throw new RuntimeException("售后单不存在");
        }
        return convertToVO(afterSale);
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Long userId = UserHolder.getUser().getId();
        AfterSale afterSale = getById(id);
        if (afterSale == null) {
            throw new RuntimeException("售后单不存在");
        }
        if (!afterSale.getBuyerId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        if (afterSale.getStatus() != 0) {
            throw new RuntimeException("售后状态不支持撤销");
        }
        lambdaUpdate()
                .eq(AfterSale::getId, id)
                .eq(AfterSale::getStatus, 0)
                .set(AfterSale::getStatus, 3)
                .set(AfterSale::getUpdateTime, LocalDateTime.now())
                .update();
    }

    private AfterSaleVO convertToVO(AfterSale afterSale) {
        AfterSaleVO vo = new AfterSaleVO();
        vo.setId(afterSale.getId());
        vo.setOrderNo(afterSale.getOrderNo());
        vo.setOrderId(afterSale.getOrderId());
        vo.setType(afterSale.getType());
        vo.setTypeName(afterSale.getType() == 1 ? "仅退款" : "退货退款");
        vo.setReason(afterSale.getReason());
        vo.setDescription(afterSale.getDescription());
        if (afterSale.getImages() != null && !afterSale.getImages().isBlank()) {
            vo.setImages(Arrays.asList(afterSale.getImages().split(",")));
        }
        vo.setRefundAmount(afterSale.getRefundAmount());
        vo.setStatus(afterSale.getStatus());
        vo.setStatusName(getStatusName(afterSale.getStatus()));
        vo.setSellerReply(afterSale.getSellerReply());
        vo.setCreateTime(afterSale.getCreateTime());
        vo.setUpdateTime(afterSale.getUpdateTime());
        return vo;
    }

    private String getStatusName(Integer status) {
        return switch (status) {
            case 0 -> "待处理";
            case 1 -> "卖家同意";
            case 2 -> "卖家拒绝";
            case 3 -> "买家撤销";
            case 4 -> "已完成";
            default -> "未知";
        };
    }
}
