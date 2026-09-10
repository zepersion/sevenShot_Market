package org.example.goodsservice.service.Impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.common.DTO.AllGoodsDTO.GoodsCategoryDTO;

import org.example.common.VO.GoodsAllVO.GoodsCategoryVO;
import org.example.goodsservice.entity.GoodsCategory;
import org.example.goodsservice.mapper.GoodsCategoryMapper;
import org.example.goodsservice.service.GoodsCategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsCategoryServiceImpl extends ServiceImpl<GoodsCategoryMapper, GoodsCategory> implements GoodsCategoryService {
        @Resource
        private GoodsCategoryMapper goodsCategoryMapper;
    @Override
    public List<GoodsCategoryVO> categoresList(GoodsCategoryDTO dto) {
        //1.一次性查出status=1已上架的部分
        LambdaQueryWrapper<GoodsCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GoodsCategory::getStatus, 1)
                .orderByAsc(GoodsCategory::getSort);
        List<GoodsCategory> allSon = goodsCategoryMapper.selectList(queryWrapper);
        //封装vo
        List<GoodsCategoryVO> voList = allSon.stream().map(
                e ->
                {
                    GoodsCategoryVO vo = new GoodsCategoryVO();
                    vo.setId(e.getId());
                    vo.setName(e.getName());
                    vo.setStatus(e.getStatus());
                    vo.setSort(e.getSort());
                    vo.setParentId(e.getParentId());
                    return vo;
                }
        ).collect(Collectors.toList());
        //一级分类找出他的子分类
        List<GoodsCategoryVO> rootList = new ArrayList<>();
        for (GoodsCategoryVO vo : voList) {
            if(vo.getParentId() == 0){
               rootList.add(vo);
            }
        }
        //
        addBuildTree(rootList,voList);
    return rootList;
    }
    // nodeList：当前层级的节点（一开始传入一级分类rootList）
// allVoList：完整所有分类集合，用来查找子节点
    private void addBuildTree(List<GoodsCategoryVO> rootList, List<GoodsCategoryVO> voList) {
      //遍历当前层级的每一个分类节点
        for (GoodsCategoryVO node : rootList) {
            //存放当前节点存放的子分类
          List<GoodsCategoryVO> children = new ArrayList<>();
            //在全部分类中当前节点的子节点
            for(GoodsCategoryVO vo :  voList){
                if(vo.getParentId().equals(node.getId())){
                    children.add(vo);
                    //vo的父id = 当前节点id → vo是node的子分类
                }
                }
            //给当前节点挂载所有子分类
            node.setChildren(children);
            //存在子分类，继续递归，查找子分类的下级节点
            if(!children.isEmpty()){
                addBuildTree(children, voList);
            }
        }
        }
}
