package com.shuzhi.cache.annotation;

import com.shuzhi.cache.SZCacheTypeEnum;

import java.lang.annotation.*;

import static com.shuzhi.cache.SZCacheTypeEnum.CACHE_TYPE_NORMAL;

/**
 *
 * @author wangxingzhe
 * @date 2015/4/4
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SZCacheConfig {
    /**
     * 类型
     * @return
     */
    SZCacheTypeEnum type() default CACHE_TYPE_NORMAL;

    /**
     * hash模式下，字段名称
     * @return
     */
    String[] fields() default "";

    /**
     * hash模式下，字段名称,此属性用于set缓存数据时,填充哪些filed.
     * 而上面的field用于指定get时获取哪些field。
     * @return
     */
    String[] cacheFields() default "";
    /**
     * 超时时间
     * @return
     */
    int expire() default 86400;

    /**
     * incrBy的时候，key是否必须存在
     * @return
     */
    boolean needExist() default false;

    long minValue() default Long.MIN_VALUE;

    boolean useGzip() default false;

    /**
     * 是否缓存空数据
     */
    boolean forceCacheEmpty() default false;
}
