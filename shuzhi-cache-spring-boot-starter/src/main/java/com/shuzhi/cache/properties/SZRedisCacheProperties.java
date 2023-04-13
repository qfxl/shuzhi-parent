package com.shuzhi.cache.properties;

import com.shuzhi.cache.SZCacheConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author xuyonghong
 * @date 2023-04-11 14:56
 **/
@Data
@ConfigurationProperties(prefix = "shuzhi.cache.redis")
public class SZRedisCacheProperties {

    private Boolean enabled = true;

    private String hosts;

    private String pass;

    private String keyPrefix = SZCacheConstant.CACHE_DEFAULT_PREFIX;

    private RedisPool pool = new RedisPool();

    @Data
    public static class RedisPool {
        private int maxTotal = SZCacheConstant.REDIS_POOL_MAX_TOTAL;
        private long maxWaitMillis = SZCacheConstant.REDIS_POOL_MAX_WAIT_MILLS;
        private int maxIdle = SZCacheConstant.REDIS_POOL_MAX_IDLE;
        private int minIdle = SZCacheConstant.REDIS_POOL_MIN_IDLE;
    }
}
