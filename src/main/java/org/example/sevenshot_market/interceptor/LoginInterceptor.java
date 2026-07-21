package org.example.sevenshot_market.interceptor;


import cn.hutool.core.bean.BeanUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.example.sevenshot_market.dto.UserDto;
import org.example.sevenshot_market.utils.UserHolder;
import org.springframework.beans.BeanUtils;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        Object userObj = request.getSession().getAttribute("loginUser");//
        if(userObj == null){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        //放到UserHolder中
        UserDto user = BeanUtil.toBean(userObj, UserDto.class);
        UserHolder.saveUser(user);

        return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex){
        UserHolder.removeUser();
    }
}
