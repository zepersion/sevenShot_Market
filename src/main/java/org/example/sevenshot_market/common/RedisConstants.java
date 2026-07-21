package org.example.sevenshot_market.common;

public class RedisConstants {
    public static final String SEND_CODE_KEY = "code";
    public static final Long SEND_CODE_TTL = 3L;
    // 登录token
    public static final String LOGIN_TOKEN_KEY = "login:token:";
    public static final Long LOGIN_TOKEN_TTL = 30L;
}
