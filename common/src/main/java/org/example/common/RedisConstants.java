package org.example.common;

public class RedisConstants {
    public static final String SEND_CODE_KEY = "code";
    public static final Long SEND_CODE_TTL = 3L;
    // 登录token
    public static final String LOGIN_TOKEN_KEY = "login:token:";
    public static final Long LOGIN_TOKEN_TTL = 30L;

    //签到
    public static final String USER_SIGN_KEY = "sign:user:";
    public static final Long USER_SIGN_TTL = 30L;
    //签到的连续日期
    public static final String USER_CONTINUESIGN_KEY = "user:continuous:";
    //关注和粉丝
    public static final String USER_FOLLOW_KEY = "user:follow:";
    public static final String USER_FANS_KEY = "user:fans:";
    //商品信息
    public static final String GOODS_INFO_KEY = "goods:info:";
    public static final Long GOODS_INFO_TTL = 30L;
    public static final String LOCK_GOODS_KEY="lock:goods:";
    //商品点赞
    public static final String GOODS_LIKE_KEY = "like:goods:";
    public static final String GOODS_FAVORITE_SET = "goods:favorite:";
    //收藏
    public static final String GOODS_FAVORITE_KEY = "favorite:goods:";
    //商品热度
    public static final String GOODS_RANKS_HOT_KEY = "goods:ranksing:hot:";
}
