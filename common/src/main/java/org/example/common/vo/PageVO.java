package org.example.common.vo;

import lombok.Data;

import java.util.List;

@Data
public class PageVO<T> {
    // 当前页码
    private Long current;
    // 总数据条数
    private Long total;
    // 总页数
    private Long pages;
    // 当前页数据列表（泛型T，可以是UserFollowVO）
    private List<T> records;
    private Long size;
}
