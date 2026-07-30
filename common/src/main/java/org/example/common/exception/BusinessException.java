package org.example.common.exception;

import lombok.Getter;

@Getter//自动给类中所有私有字段生成 getXxx() 方法，省去手动写 get 方法的重复代码。
public class BusinessException extends RuntimeException {
    private  final  Integer code;

    //自定义业务逻辑错误
    public BusinessException(String message, Integer code) {
        super(message);
        this.code = code;
    }
    //业务逻辑错误通常的状态码400
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }
}
