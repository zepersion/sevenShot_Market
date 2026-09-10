package org.example.common.utils;

import org.example.common.DTO.AllUserDTO.UserDTO;


//userholder相当于我们在线程中有一个存独立资料的抽屉
public class UserHolder {
    //静态threadlocal 存脱敏后的用户
    private static final ThreadLocal<UserDTO> THREAD_LOCAL_USER = new ThreadLocal<>();
        //存用户信息
    public static void saveUser(UserDTO userDto){
        THREAD_LOCAL_USER.set(userDto);
    }
    //获取用户信息
    public static UserDTO getUser() {
        return THREAD_LOCAL_USER.get();
    }
    //获取用户登陆id
    public static Long getUserId(UserDTO userDto){
        UserDTO user = getUser();
        // 未登录时返回null，拦截器已经提前拦截，正常业务不会走到这里
        return user == null ? null :user.getId();
    }

    //清除线程数据
    public static void removeUser(){
        THREAD_LOCAL_USER.remove();
    }
}
