package com.shuzhi.cache.core;

import com.shuzhi.cache.core.pojo.SZCacheConfigBean;
import org.springframework.cache.Cache;

/**
 *
 * @author wangxingzhe
 * @date 2015/4/4
 */
public interface SZCache extends Cache {
    /**
     * get valueMapper
     * @param cacheConfig
     * @param key
     * @return
     */
    ValueWrapper get(SZCacheConfigBean cacheConfig, Object key);

    /**
     * put cacheConfig
     * @param cacheConfig
     * @param key
     * @param value
     */
    void put(SZCacheConfigBean cacheConfig, Object key, Object value);

    /**
     * evict cacheConfig
     * @param cacheConfig
     * @param key
     */
    void evict(SZCacheConfigBean cacheConfig, Object key);

    /**
     * clear cacheConfig
     * @param cacheConfig
     */
    void clear(SZCacheConfigBean cacheConfig);
}
