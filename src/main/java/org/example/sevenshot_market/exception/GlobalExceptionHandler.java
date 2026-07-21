package org.example.sevenshot_market.exception;

import lombok.extern.slf4j.Slf4j;

import org.example.sevenshot_market.common.Result;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.BindException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
//捕获业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleException(BusinessException e){
        log.warn("业务异常:{}",e.getMessage());
        return Result.error(e.getCode(),e.getMessage());
    }
/*作用：只捕获参数校验失败自动抛出的两种异常
MethodArgumentNotValidException：前端传 JSON 请求体（@RequestBody），DTO 校验失败抛出
BindException：前端传表单、地址栏参数（@RequestParam）校验失败抛出
只要参数不符合 @NotBlank、@NotNull、@Pattern 等校验规则，Spring 自动抛出上面其中一个异常，自动进入这个方法处理。*/
@ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public  Result<Void> handleValidException(Exception e){
        String msg = e.getMessage();
        if(e instanceof MethodArgumentNotValidException ex  &&ex.getBindingResult().getFieldError()!=null){
                /*3. 重点代码分步解释
① e instanceof MethodArgumentNotValidException ex
instanceof：判断当前异常对象 e 是不是 JSON 参数校验异常
如果是，直接强转为 MethodArgumentNotValidException 类型变量 ex，方便调用专属方法。
② ex.getBindingResult()
拿到本次所有参数校验的错误结果集合。
③ .getFieldError()
获取第一个校验失败的字段信息（字段名、错误提示）。
④ .getDefaultMessage()*/
            msg = ex.getBindingResult().getFieldError().getDefaultMessage();
        }
        return Result.error(400,msg);}
@ExceptionHandler(Exception.class)
 public Result<Void> exception(Exception  e){
        log.error("系统处理异常",e);
        return Result.error(500,"系统出现异常,请联系管理员");
}




}
