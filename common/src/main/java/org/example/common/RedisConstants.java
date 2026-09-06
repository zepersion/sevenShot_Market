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
    //秒杀
    public static final String SECKILL_LIST_KEY="seckill:list:";
    public static final String SECKILL_SOLDCOUNT_KEY="seckill:sold:";
    public static final String SECKILL_LIMITER="seckill:limit:";
    public static final String SECKILL_STOCK_KEY="seckill:stock:";
    public static final String SECKILL_INFO_KEY="seckill:info:";
    public static final String SECKILL_STARTTIME_KEY="seckill:starttime:";
    public static final String SECKILL_ENDTTIME_KEY="seckill:endtime:";
    public static final String SECKILL_USER_SET_KEY = "seckill:user:";
    public static final String USER_KEY = "user:";

    // 订单缓存
    public static final String ORDER_CACHE_KEY = "order:cache:";
    public static final Long ORDER_CACHE_TTL = 30L; // 30分钟
    // 订单分布式锁
    public static final String ORDER_LOCK_KEY = "lock:order:";
    // 订单号自增序列
    public static final String ORDER_SEQ_KEY = "order:seq:";
    //订单详情key
    public static final String ORDER_INFO_KEY = "order:info:";
    public static final String ORDER_GOODS_INFO_KEY="order:goods:info:";
    public static final String ORDER_GOODS_BUYER_KEY="order:goods:buyer:";
    public static final String ORDER_GOODS_SELLER_KEY="order:goods:seller:";
    public static final String ORDER_CACHE_ID_KEY = "order:cache:";

    // 悬赏浏览量
    public static final String REWARD_VIEW_KEY = "reward:view:";
    public static final String REWARD_VIEW_USER_KEY = "reward:view:user:";
    // 悬赏分布式锁
    public static final String REWARD_LOCK_KEY = "lock:reward:";
    // 悬赏缓存
    public static final String REWARD_INFO_KEY = "reward:info:";
    public static final Long REWARD_INFO_TTL = 30L;
}

