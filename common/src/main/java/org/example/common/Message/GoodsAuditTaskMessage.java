package org.example.common.Message;

import lombok.Data;

import java.util.List;

@Data
public class GoodsAuditTaskMessage {
    private Long goodsId;
    private String Title;
    private String Description;
    private List Tags;
}
