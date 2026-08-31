package org.example.common.Message;

import lombok.Data;

import java.util.List;

@Data
public class OrderDelayMessage {
    //订单id
    private Long orderId;
    //millList
    private List<Long> delaysMillList;
    //商品id
    private Long goodsId;

    public Long getNextDelay(){
        if(delaysMillList==null ||delaysMillList.isEmpty()){
            return null;
        }
        return delaysMillList.remove(0);
    }
    public Boolean hasMoreDelay(){
        return delaysMillList!=null && !delaysMillList.isEmpty();
    }

}
