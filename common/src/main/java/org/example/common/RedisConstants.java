package org.example.common;

public class RedisConstants {
    public static final String SEND_CODE_KEY = "code";
    public static final Long SEND_CODE_TTL = 3L;
    // 登录token
    public static final String LOGIN_TOKEN_KEY = "login:token:";
    public static final Long LOGIN_TOKEN_TTL = 30L;

    //签到
    public static final String USER_SIGN_KEY = "sign:";
    public static final Long USER_SIGN_TTL = 30L;
    //签到的连续日期
    public static final String USER_CONTINUESIGN_KEY = "user:info:";
}
