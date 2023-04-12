package com.shuzhi.cache.autoconfig;

import com.shuzhi.cache.SZCacheConstant;
import com.shuzhi.cache.core.interceptor.SZCacheInterceptor;
import com.shuzhi.cache.core.service.ICacheService;
import com.shuzhi.cache.core.service.RedisService;
import com.shuzhi.cache.core.service.impl.RedisCacheServiceImpl;
import com.shuzhi.cache.core.support.SZCacheSupport;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
public class SZCacheAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "shuzhi.cache", name = "cacheType", havingValue = "redis")
    public ICacheService getRedisCacheService() {
        return new RedisCacheServiceImpl();
    }

    @Bean
    @ConditionalOnProperty(prefix = "shuzhi.cache", name = "cacheType", havingValue = "redis")
    public RedisService getRedisService() {
        return new RedisService();
    }

    @Bean
    public CacheManager cacheManager(ICacheService cacheService) {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        SZCacheSupport cacheSupport = new SZCacheSupport();
        cacheSupport.setExpireTime(SZCacheConstant.CACHE_DEFAULT_EXPIRE_TIME);
        cacheSupport.setName(SZCacheConstant.CACHE_DEFAULT_NAME);
        cacheSupport.setCacheService(cacheService);
        cacheManager.setCaches(Collections.singletonList(cacheSupport));
        return cacheManager;
    }

    @Bean
    public SZCacheInterceptor cacheInterceptor(CacheManager cacheManager) {
        SZCacheInterceptor interceptor = new SZCacheInterceptor();
        interceptor.setCacheManager(cacheManager);
        interceptor.setCacheOperationSources(new AnnotationCacheOperationSource());
        return interceptor;
    }

    @Bean
    public Advisor cacheAdvisor(SZCacheInterceptor cacheInterceptor) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        String expression = getCacheExpression();
        System.out.println("expression is " + expression);
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