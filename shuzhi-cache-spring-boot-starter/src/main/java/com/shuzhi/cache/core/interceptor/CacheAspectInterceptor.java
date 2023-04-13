/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shuzhi.cache.core.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.shuzhi.cache.SZCacheTypeEnum;
import com.shuzhi.cache.annotation.SZCacheConfig;
import com.shuzhi.cache.core.common.DefaultKeyGenerator;
import com.shuzhi.cache.core.common.ExpressionEvaluator;
import com.shuzhi.cache.core.pojo.SZCacheConfigBean;
import com.shuzhi.cache.core.support.CacheSupport;
import lombok.EqualsAndHashCode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.*;
import org.springframework.expression.EvaluationContext;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * <p>This enables the underlying Spring caching infrastructure to be
 * used easily to implement an aspect for any aspect system.
 *
 * <p>Subclasses are responsible for calling methods in this class in
 * the correct order.
 *
 * <p>Uses the <b>Strategy</b> design pattern. A {@link CacheManager}
 * implementation will perform the actual cache management, and a
 * operations.
 *
 * <p>A cache aspect is serializable if its {@code CacheManager} and
 * {@code CacheOperationSource} are serializable.
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 3.1
 */
public abstract class CacheAspectInterceptor implements InitializingBean {

    public interface Invoker {
        /**
         * invoke
         * @return
         */
        Object invoke();
    }

    protected final Log logger = LogFactory.getLog(getClass());

    private CacheManager cacheManager;

    private CacheOperationSource cacheOperationSource;

    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();

    private KeyGenerator keyGenerator = new DefaultKeyGenerator();

    private boolean initialized = false;

    private static final String CACHEABLE = "cacheable", UPDATE = "cacheupdate", EVICT = "cacheevict";

    /**
     * Set the CacheManager that this cache aspect should delegate to.
     */
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Return the CacheManager that this cache aspect delegates to.
     */
    public CacheManager getCacheManager() {
        return this.cacheManager;
    }

    /**
     * Set one or more cache operation sources which are used to find the cache
     * attributes. If more than one source is provided, they will be aggregated using a
     *
     * @param cacheOperationSources must not be {@code null}
     */
    public void setCacheOperationSources(CacheOperationSource... cacheOperationSources) {
        Assert.notEmpty(cacheOperationSources, "cacheOperationSources must not be null");
        this.cacheOperationSource =
                (cacheOperationSources.length > 1 ?
                        new CompositeCacheOperationSource(cacheOperationSources) :
                        cacheOperationSources[0]);
    }

    /**
     * Return the CacheOperationSource for this cache aspect.
     */
    public CacheOperationSource getCacheOperationSource() {
        return this.cacheOperationSource;
    }

    /**
     * Set the KeyGenerator for this cache aspect.
     */
    public void setKeyGenerator(KeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    /**
     * Return the KeyGenerator for this cache aspect,
     */
    public KeyGenerator getKeyGenerator() {
        return this.keyGenerator;
    }

    @Override
    public void afterPropertiesSet() {
        if (this.cacheManager == null) {
            throw new IllegalStateException("'cacheManager' is required");
        }
        if (this.cacheOperationSource == null) {
            throw new IllegalStateException("The 'cacheOperationSources' property is required: "
                    + "If there are no cacheable methods, then don't use a cache aspect.");
        }

        this.initialized = true;
    }

    /**
     * Convenience method to return a String representation of this Method
     * for use in logging. Can be overridden in subclasses to provide a
     * different identifier for the given method.
     *
     * @param method      the method we're interested in
     * @param targetClass class the method is on
     * @return log message identifying this method
     * @see ClassUtils#getQualifiedMethodName
     */
    protected String methodIdentification(Method method, Class<?> targetClass) {
        Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
        return ClassUtils.getQualifiedMethodName(specificMethod);
    }

    protected Collection<Cache> getCaches(CacheOperation operation) {
        Set<String> cacheNames = operation.getCacheNames();
        Collection<Cache> caches = new ArrayList<Cache>(cacheNames.size());
        for (String cacheName : cacheNames) {
            Cache cache = this.cacheManager.getCache(cacheName);
            if (cache == null) {
                throw new IllegalArgumentException("Cannot find cache named [" + cacheName + "] for " + operation);
            }
            caches.add(cache);
        }
        return caches;
    }

    protected CacheOperationContext getOperationContext(CacheOperation operation, Method method, Object[] args,
                                                        Object target, Class<?> targetClass) {

        return new CacheOperationContext(operation, method, args, target, targetClass);
    }

    protected Object execute(Invoker invoker, Object target, Method method, Object[] args) {
        // check whether aspect is enabled
        // to cope with cases where the AJ is pulled in automatically
        if (!this.initialized) {
            return invoker.invoke();
        }

        // get backing class
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
        final Collection<CacheOperation> cacheOp = getCacheOperationSource().getCacheOperations(method, targetClass);

        // analyze caching information
        if (!CollectionUtils.isEmpty(cacheOp)) {
            Map<String, Collection<CacheOperationContext>> ops = createOperationContext(cacheOp, method, args, target, targetClass);

            // start with evictions
            //首先执行@CacheEvict（如果beforeInvocation=true且condition 通过），如果allEntries=true，则清空所有
            inspectBeforeCacheEvicts(ops.get(EVICT));

            // follow up with cacheable
            //如果cachePutRequests为空且没有@CachePut操作，那么将查找@Cacheable的缓存，否则result=缓存数据（也就是说只要当没有cache put请求时才会查找缓存）
            CacheStatus status = inspectCacheables(ops.get(CACHEABLE));

            Object retVal;
            Map<CacheOperationContext, Object> updates = new LinkedHashMap<CacheOperationContext, Object>();

            if (status != null) {
//                收集@Cacheable（如果condition 通过，且key对应的数据不在缓存），放入cachePutRequests（也就是说如果cachePutRequests为空，则数据在缓存中）
                if (status.updateRequired) {
                    updates.putAll(status.cUpdates);
                }
                // return cached object
                else {
                    //所有类型，都尝试反序列化,如果retVal不是string类型，则直接返回。
                    if (status.retVal instanceof String) {
                        try {
                            return JSON.parseObject(status.retVal.toString(), method.getGenericReturnType(), Feature.IgnoreNotMatch);
                        } catch (Exception e) {
                            if (logger.isDebugEnabled()) {
                                logger.error("无法反序列化缓存内容：" + status.retVal, e);
                            } else {
                                logger.error("无法反序列化缓存内容：" + status.retVal);
                            }

                        }
                    } else {
                        return status.retVal;
                    }
                }
            }
            //如果没有找到缓存，那么调用实际的API，把结果放入result
            retVal = invoker.invoke();
            //如果有@CachePut操作(如果condition 通过)，那么放入cachePutRequests
            Map<CacheOperationContext, Object> cachePutUpdates = inspectCacheUpdates(ops.get(UPDATE), retVal);
            updates.putAll(cachePutUpdates);

            //如果有@CachePut操作(如果condition 通过)，那么放入cachePutRequests
            inspectAfterCacheEvicts(ops.get(EVICT));

            //执行cachePutRequests，将数据写入缓存（unless为空或者unless解析结果为false）；
            if (!updates.isEmpty()) {
                update(updates, retVal);
            }

            return retVal;
        }

        return invoker.invoke();
    }

    private void inspectBeforeCacheEvicts(Collection<CacheOperationContext> evictions) {
        inspectCacheEvicts(evictions, true);
    }

    private void inspectAfterCacheEvicts(Collection<CacheOperationContext> evictions) {
        inspectCacheEvicts(evictions, false);
    }

    private void inspectCacheEvicts(Collection<CacheOperationContext> evictions, boolean beforeInvocation) {

        if (!evictions.isEmpty()) {

            boolean log = logger.isTraceEnabled();

            for (CacheOperationContext context : evictions) {
                CacheEvictOperation evictOp = (CacheEvictOperation) context.operation;

                if (beforeInvocation == evictOp.isBeforeInvocation()) {
                    if (context.isConditionPassing()) {
                        // for each cache
                        // lazy key initialization
                        Object key = null;

                        for (Cache cache : context.getCaches()) {
                            // cache-wide flush
                            if (evictOp.isCacheWide()) {
                                if (cache instanceof CacheSupport) {
                                    CacheSupport CacheSupport = (CacheSupport) cache;
                                    CacheSupport.clear(context.getCacheConfig());
                                } else {
                                    cache.clear();
                                }
                                if (log) {
                                    logger.trace("Invalidating entire cache for operation " + evictOp + " on method " + context.method);
                                }
                            } else {
                                // check key
                                if (key == null) {
                                    key = context.generateKey();
                                }
                                if (log) {
                                    logger.trace("Invalidating cache key " + key + " for operation " + evictOp + " on method " + context.method);
                                }
                                if (CacheSupport.class.isInstance(cache)) {
                                    CacheSupport CacheSupport = (CacheSupport) cache;
                                    CacheSupport.evict(context.getCacheConfig(), key);
                                } else {
                                    cache.evict(key);
                                }
                            }
                        }
                    } else {
                        if (log) {
                            logger.trace("Cache condition failed on method " + context.method + " for operation " + context.operation);
                        }
                    }
                }
            }
        }
    }

    private CacheStatus inspectCacheables(Collection<CacheOperationContext> cacheables) {
        Map<CacheOperationContext, Object> cUpdates = new LinkedHashMap<CacheOperationContext, Object>(cacheables.size());

        boolean updateRequire = false;
        Object retVal = null;

        if (!cacheables.isEmpty()) {
            boolean log = logger.isTraceEnabled();
            boolean atLeastOnePassed = false;

            for (CacheOperationContext context : cacheables) {
                if (context.isConditionPassing()) {
                    atLeastOnePassed = true;
                    Object key = context.generateKey();

                    if (log) {
                        logger.trace("Computed cache key " + key + " for operation " + context.operation);
                    }
                    if (key == null) {
                        throw new IllegalArgumentException(
                                "Null key returned for cache operation (maybe you are using named params on classes without debug info?) "
                                        + context.operation);
                    }

                    // add op/key (in case an update is discovered later on)
                    cUpdates.put(context, key);

                    boolean localCacheHit = false;

                    // check whether the cache needs to be inspected or not (the method will be invoked anyway)
                    if (!updateRequire) {
                        for (Cache cache : context.getCaches()) {
                            Cache.ValueWrapper wrapper;
                            if (cache instanceof CacheSupport) {
                                CacheSupport cacheSupport = (CacheSupport) cache;
                                wrapper = cacheSupport.get(context.getCacheConfig(), key);
                            } else {
                                wrapper = cache.get(key);
                            }
                            if (wrapper != null) {
                                retVal = wrapper.get();
                                localCacheHit = true;
                                break;
                            }
                        }
                    }

                    if (!localCacheHit) {
                        updateRequire = true;
                    }
                } else {
                    if (log) {
                        logger.trace("Cache condition failed on method " + context.method + " for operation " + context.operation);
                    }
                }
            }

            // return a status only if at least on cacheable matched
            if (atLeastOnePassed) {
                return new CacheStatus(cUpdates, updateRequire, retVal);
            }
        }

        return null;
    }

    private static class CacheStatus {
        // caches/key
        final Map<CacheOperationContext, Object> cUpdates;
        final boolean updateRequired;
        final Object retVal;

        CacheStatus(Map<CacheOperationContext, Object> cUpdates, boolean updateRequired, Object retVal) {
            this.cUpdates = cUpdates;
            this.updateRequired = updateRequired;
            this.retVal = retVal;
        }
    }

    private Map<CacheOperationContext, Object> inspectCacheUpdates(Collection<CacheOperationContext> updates, Object retVal) {

        Map<CacheOperationContext, Object> cUpdates = new LinkedHashMap<CacheOperationContext, Object>(updates.size());

        if (!updates.isEmpty()) {
            boolean log = logger.isTraceEnabled();

            for (CacheOperationContext context : updates) {
                context.setVariable("result", retVal);
                if (context.isConditionPassing()) {

                    Object key = context.generateKey();

                    if (log) {
                        logger.trace("Computed cache key " + key + " for operation " + context.operation);
                    }
                    if (key == null) {
                        throw new IllegalArgumentException(
                                "Null key returned for cache operation (maybe you are using named params on classes without debug info?) "
                                        + context.operation);
                    }

                    // add op/key (in case an update is discovered later on)
                    cUpdates.put(context, key);
                } else {
                    if (log) {
                        logger.trace("Cache condition failed on method " + context.method + " for operation " + context.operation);
                    }
                }
            }
        }

        return cUpdates;
    }

    private void update(Map<CacheOperationContext, Object> updates, Object retVal) {
        for (Map.Entry<CacheOperationContext, Object> entry : updates.entrySet()) {
            for (Cache cache : entry.getKey().getCaches()) {
                if (cache instanceof CacheSupport) {
                    CacheSupport CacheSupport = (CacheSupport) cache;
                    CacheSupport.put(entry.getKey().getCacheConfig(), entry.getValue(), retVal);
                } else {
                    cache.put(entry.getValue(), retVal);
                }
            }
        }
    }

    private Map<String, Collection<CacheOperationContext>> createOperationContext(Collection<CacheOperation> cacheOp,
                                                                                  Method method, Object[] args, Object target, Class<?> targetClass) {
        Map<String, Collection<CacheOperationContext>> map = new LinkedHashMap<>(3);

        Collection<CacheOperationContext> cacheables = new ArrayList<>();
        Collection<CacheOperationContext> evicts = new ArrayList<>();
        Collection<CacheOperationContext> updates = new ArrayList<>();

        for (CacheOperation cacheOperation : cacheOp) {
            CacheOperationContext opContext = getOperationContext(cacheOperation, method, args, target, targetClass);

            if (cacheOperation instanceof CacheableOperation) {
                cacheables.add(opContext);
            }

            if (cacheOperation instanceof CacheEvictOperation) {
                evicts.add(opContext);
            }

            if (cacheOperation instanceof CachePutOperation) {
                updates.add(opContext);
            }
        }

        map.put(CACHEABLE, cacheables);
        map.put(EVICT, evicts);
        map.put(UPDATE, updates);

        return map;
    }

    @EqualsAndHashCode
    protected class CacheOperationContext {

        private final CacheOperation operation;

        private final Collection<Cache> caches;

        private final Object target;

        private final Method method;

        private final Object[] args;

        private final SZCacheConfigBean cacheConfigBean;

        // context passed around to avoid multiple creations
        private final EvaluationContext evalContext;

        public CacheOperationContext(CacheOperation operation, Method method, Object[] args, Object target, Class<?> targetClass) {
            this.operation = operation;
            this.caches = CacheAspectInterceptor.this.getCaches(operation);
            this.target = target;
            this.method = method;
            this.args = args;
            SZCacheConfig cacheConfig = method.getAnnotation(SZCacheConfig.class);
            if (cacheConfig != null) {
                SZCacheConfigBean cacheConfigBean = new SZCacheConfigBean();
                cacheConfigBean.setType(cacheConfig.type());
                cacheConfigBean.setExpire(cacheConfig.expire());
                cacheConfigBean.setFields(cacheConfig.fields());
                cacheConfigBean.setCacheFields(cacheConfig.cacheFields());
                cacheConfigBean.setReturnType(method.getGenericReturnType());
                cacheConfigBean.setNeedExist(cacheConfig.needExist());
                cacheConfigBean.setMinValue(cacheConfig.minValue());
                cacheConfigBean.setUseGzip(cacheConfig.useGzip());
                cacheConfigBean.setForceCacheEmpty(cacheConfig.forceCacheEmpty());
                this.cacheConfigBean = cacheConfigBean;
            } else {
                this.cacheConfigBean = null;
            }

            this.evalContext = evaluator.createEvaluationContext(caches, method, args, target, targetClass);
        }

        protected boolean isConditionPassing() {
            if (StringUtils.hasText(this.operation.getCondition())) {
                return evaluator.condition(this.operation.getCondition(), this.method, this.evalContext);
            }
            return true;
        }

        /**
         * Computes the key for the given caching operation.
         *
         * @return generated key (null if none can be generated)
         */
        protected Object generateKey() {
            if (StringUtils.hasText(this.operation.getKey())) {
                //计算filed并且只有CACHE_TYPE_HASH_ONE类型的才解析field里面的表达式
                if (this.cacheConfigBean != null && this.cacheConfigBean.getType() == SZCacheTypeEnum.CACHE_TYPE_HASH_ONE) {
                    String[] fields = this.cacheConfigBean.getFields();
                    if (fields != null) {
                        for (int i = 0; i < fields.length; i++) {
                            if (StringUtils.hasText(fields[i])) {
                                Object field = evaluator.field(fields[i], this.method, this.evalContext);
                                if (field != null) {
                                    this.cacheConfigBean.setFields(i, field.toString());
                                }
                            }
                        }
                    }
                }
                return evaluator.key(this.operation.getKey(), this.method, this.evalContext);
            }
            return keyGenerator.generate(this.target, this.method, this.args);
        }

        protected Collection<Cache> getCaches() {
            return this.caches;
        }

        protected void setVariable(String key, Object value) {
            this.evalContext.setVariable(key, value);
        }

        protected SZCacheConfigBean getCacheConfig() {
            return this.cacheConfigBean;
        }
    }
}