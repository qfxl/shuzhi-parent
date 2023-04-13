package com.shuzhi.cache.autoconfig;

import com.shuzhi.cache.SZCacheConstant;
import com.shuzhi.cache.core.interceptor.SZCacheAspectInterceptor;
import com.shuzhi.cache.core.service.ICacheService;
import com.shuzhi.cache.core.support.SZCacheSupport;
import com.shuzhi.cache.properties.SZRedisCacheProperties;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.Collections;

/**
 * @author xuyonghong
 * @date 2023-04-11 19:56
 **/
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties(SZRedisCacheProperties.class)
public class SZCacheAutoConfiguration {

    private final SZRedisCacheProperties szRedisCacheProperties;

    public SZCacheAutoConfiguration(SZRedisCacheProperties szRedisCacheProperties) {
        this.szRedisCacheProperties = szRedisCacheProperties;
    }

    @Bean
    @ConditionalOnBean(ICacheService.class)
    public CacheManager cacheManager(ICacheService cacheService) {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        SZCacheSupport cacheSupport = new SZCacheSupport();
        cacheSupport.setExpireTime(SZCacheConstant.CACHE_DEFAULT_EXPIRE_TIME);
        cacheSupport.setName(SZCacheConstant.CACHE_DEFAULT_PREFIX);
        cacheSupport.setCacheService(cacheService);
        cacheSupport.setEnableCache(szRedisCacheProperties.getEnabled());
        cacheManager.setCaches(Collections.singletonList(cacheSupport));
        return cacheManager;
    }

    @Bean
    @ConditionalOnBean(CacheManager.class)
    public SZCacheAspectInterceptor cacheInterceptor(CacheManager cacheManager) {
        SZCacheAspectInterceptor interceptor = new SZCacheAspectInterceptor();
        interceptor.setCacheManager(cacheManager);
        interceptor.setCacheOperationSources(new AnnotationCacheOperationSource());
        return interceptor;
    }

    @Bean
    @ConditionalOnBean(SZCacheAspectInterceptor.class)
    public Advisor cacheAdvisor(SZCacheAspectInterceptor cacheInterceptor) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        String expression = getCacheExpression();
        pointcut.setExpression(expression);
        return new DefaultPointcutAdvisor(pointcut, cacheInterceptor);
    }

    /**
     * 获取切面表达式
     *
     * @return
     */
    private String getCacheExpression() {
        return "@annotation(org.springframework.cache.annotation.Cacheable)" +
                " || " +
                "@annotation(org.springframework.cache.annotation.CachePut)" +
                " || " +
                "@annotation(org.springframework.cache.annotation.CacheEvict)" +
                " || " +
                "@annotation(org.springframework.cache.annotation.Caching)";
    }
}