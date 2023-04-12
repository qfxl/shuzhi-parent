package com.shuzhi.cache;

/**
 * Created by wangxingzhe on 2015/4/5.
 */
public class SZCacheConstant {
    /**
     * 默认缓存前缀
     */
    public static final String CACHE_DEFAULT_NAME = "szCache";
    /**
     * 缓存默认过期时间
     */
    public static final int CACHE_DEFAULT_EXPIRE_TIME = 7 * 24 * 3600;

    public static final String CACHE_GROUP = "'SZCache-Spring-Boot-Starter'";

    public static final String CACHE_TYPE_REDIS = "redis";
}
