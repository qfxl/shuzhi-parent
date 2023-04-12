package com.shuzhi.cache;

/**
 *
 * @author wangxingzhe
 * @date 2015/4/4
 */
public enum SZCacheTypeEnum {
    /**
     * 普通
     */
    CACHE_TYPE_NORMAL,
    /**
     * 递增
     */
    CACHE_TYPE_INCR,
    /**
     * 递增by返回值
     */
    CACHE_TYPE_INCR_BY,
    /**
     * hash 单个key操作
     * 会读取field属性
     */
    CACHE_TYPE_HASH_ONE,

    /**
     * hash递增
     */
    CACHE_TYPE_HASH_INCR,
    /**
     * hash 多个key操作，按照返回对象中的属性去填充或者获取缓存
     * 会读取fields属性
     */
    CACHE_TYPE_HASH_MULTI,
    /**
     * list
     */
    CACHE_TYPE_LIST,
    /**
     * sorted_set
     */
    CACHE_TYPE_SORTED_SET
}
