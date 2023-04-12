package com.shuzhi.cache.settings;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author xuyonghong
 * @date 2023-04-11 14:56
 **/
@Data
@Component
@ConfigurationProperties(prefix = "shuzhi.cache")
public class SZCacheSettings {

    private Boolean enabled;

    private String cacheType;

    @Value("${redis.hosts}")
    private String hosts;
    @Value("${redis.keyPrefix}")
    private String keyPrefix;
    @Value("${redis.pool.maxTotal}")
    private int maxTotal;
    @Value("${redis.pool.maxIdle}")
    private int maxIdle;
    @Value("${redis.pool.maxWaitMillis}")
    private int maxWaitMillis;
    @Value("${redis.pool.minIdle}")
    private int minIdle;
    @Value("${redis.pass}")
    private String password;
}
