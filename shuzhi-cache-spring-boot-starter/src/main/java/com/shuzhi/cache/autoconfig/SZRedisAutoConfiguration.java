package com.shuzhi.cache.autoconfig;

import com.shuzhi.cache.core.cache.SZRedisCache;
import com.shuzhi.cache.core.service.ICacheService;
import com.shuzhi.cache.core.service.impl.RedisCacheServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xuyonghong
 * @date 2023-04-13 14:12
 **/
@Configuration
@AutoConfigureBefore(SZCacheAutoConfiguration.class)
@ConditionalOnProperty(prefix = "shuzhi.cache", name = "cacheType", havingValue = "redis")
public class SZRedisAutoConfiguration {

    @Bean
    public ICacheService getRedisCacheService() {
        return new RedisCacheServiceImpl();
    }

    @Bean
    public SZRedisCache getRedisService() {
        return new SZRedisCache();
    }
}

