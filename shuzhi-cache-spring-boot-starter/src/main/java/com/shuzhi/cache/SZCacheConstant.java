package com.shuzhi.cache;

/**
 * Created by wangxingzhe on 2015/4/5.
 */
public class SZCacheConstant {
    /**
     * 默认缓存前缀
     */
    public static final String CACHE_DEFAULT_PREFIX = "sz-";
    /**
     * 缓存默认过期时间
     */
    public static final int CACHE_DEFAULT_EXPIRE_TIME = 7 * 24 * 3600;
    /**
     * Redis缓存
     */
    public static final String CACHE_TYPE_REDIS = "redis";

    /**
     * redis pool设置
     */
    public static final int REDIS_POOL_MAX_TOTAL = 10;
    public static final long REDIS_POOL_MAX_WAIT_MILLS = -1;
    public static final int REDIS_POOL_MAX_IDLE = 8;
    public static final int REDIS_POOL_MIN_IDLE = 0;


}
