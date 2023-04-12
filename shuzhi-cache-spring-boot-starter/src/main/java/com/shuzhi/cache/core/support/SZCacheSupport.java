package com.shuzhi.cache.core.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.shuzhi.cache.SZCacheTypeEnum;
import com.shuzhi.cache.core.SZCache;
import com.shuzhi.cache.core.codec.LongCodec;
import com.shuzhi.cache.core.common.StringSerializeUtil;
import com.shuzhi.cache.core.pojo.SZCacheConfigBean;
import com.shuzhi.cache.core.service.ICacheService;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author wangxingzhe
 * @date 2015/3/31
 */
public class SZCacheSupport implements SZCache {

    protected static Logger logger = LoggerFactory.getLogger(SZCacheSupport.class);

    private String name;

    private int expireTime = 0;

    @Value("${redis.enable}")
    private Boolean enable;

    private ICacheService cacheService;

    static {
        SerializeConfig.getGlobalInstance().put(Long.class, new LongCodec());
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Object getNativeCache() {
        return cacheService;
    }

    /**
     * 默认类型的get操作
     *
     * @param key
     * @return
     */
    @Override
    public ValueWrapper get(Object key) {
        return this.get(null, key);
    }

    @Override
    public <T> T get(Object o, Class<T> aClass) {
        return null;
    }

    @Override
    public <T> T get(Object o, Callable<T> callable) {
        return null;
    }

    /**
     * 获取
     *
     * @param key
     * @return
     */
    @Override
    public ValueWrapper get(SZCacheConfigBean cacheConfig, Object key) {
        if (Boolean.FALSE.equals(enable)) {
            return null;
        }
        ValueWrapper valueWrapper = null;
        if (key != null) {
            String k = key.toString();
            String ret = null;
            if (cacheConfig == null) {
                ret = cacheService.get(k);
                if (StringUtils.hasText(ret)) {
                    valueWrapper = new SimpleValueWrapper(ret);
                }
            } else {
                switch (cacheConfig.getType()) {
                    case CACHE_TYPE_HASH_ONE:
                    case CACHE_TYPE_HASH_INCR:
                        if (cacheConfig.getFields() != null
                                && cacheConfig.getFields().length > 0
                                && StringUtils.hasText(cacheConfig.getFields()[0])) {
                            ret = cacheService.hget(k, cacheConfig.getFields()[0]);
                        } else {
                            throw new IllegalArgumentException(
                                    "hGet field is null "
                                            + Arrays.toString(cacheConfig.getFields()));
                        }
                        break;
                    case CACHE_TYPE_HASH_MULTI:
                        if (cacheConfig.getFields() != null
                                && cacheConfig.getFields().length > 0) {
                            String[] fields = cacheConfig.getFields();

                            try {
                                Class<?> retType = (Class<?>) cacheConfig.getReturnType();
                                Object obj = retType.newInstance();
                                List<String> hdata = cacheService.hmget(k, cacheConfig.getFields());
                                boolean hasValue = false;
                                for (int i = 0; i < fields.length; i++) {
                                    String data = hdata.get(i);
                                    if (!StringUtils.hasText(data)) {
                                        continue;
                                    }
                                    hasValue = true;
                                    if (StringUtils.hasText(data)) {
                                        Field field = FieldUtils.getField(retType, fields[i], true);
                                        if (field != null) {
                                            Type fieldType = field.getGenericType();
                                            //只有非标准类型和string类型，才会序列化。
                                            if (((Class) field.getGenericType()).isPrimitive() || String.class.isAssignableFrom((Class<?>) fieldType)) {
                                                BeanUtils.setProperty(obj, fields[i], data);
                                            } else {
                                                Object dataObj = JSON.parseObject(data, fieldType);
                                                BeanUtils.setProperty(obj, fields[i], dataObj);
                                            }
                                        }
                                    }
                                }
                                if (hasValue) {
                                    valueWrapper = new SimpleValueWrapper(obj);
                                }
                            } catch (Exception e) {
                                logger.error("转换类型错误!", e);
                            }
                        } else {
                            throw new IllegalArgumentException(
                                    "hGet field is null "
                                            + Arrays.toString(cacheConfig.getFields()));
                        }
                        break;
                    case CACHE_TYPE_LIST:
                        throw new IllegalArgumentException(
                                "list cache has not implemented !"
                                        + Arrays.toString(cacheConfig.getFields()));
                    case CACHE_TYPE_SORTED_SET:
                        throw new IllegalArgumentException(
                                "sorted set cache has not implemented !"
                                        + Arrays.toString(cacheConfig.getFields()));
                    default:
                        ret = cacheService.get(k);
                }
                if (!StringUtils.isEmpty(ret) && cacheConfig.isUseGzip()) {
                    ret = StringSerializeUtil.deserializeJSONFromGzipString(ret);
                }
                if (StringUtils.hasText(ret) && cacheConfig.getType() != SZCacheTypeEnum.CACHE_TYPE_HASH_MULTI) {
                    valueWrapper = new SimpleValueWrapper(ret);
                }
            }
        }
        return valueWrapper;
    }

    @Override
    public void put(Object key, Object value) {
        this.put(null, key, value);
    }

    @Override
    public ValueWrapper putIfAbsent(Object o, Object o1) {
        return null;
    }

    @Override
    public void put(SZCacheConfigBean cacheConfig, Object key, Object value) {
        if (key != null) {
            if (!cacheConfig.isForceCacheEmpty() && (value == null
                    || (
                    (
                            (value instanceof String && !StringUtils.hasText(value.toString()))
                                    || (value instanceof Collection && ((Collection) value).size() == 0)
                    )
            ))
            ) {
                return;
            }
            String k = key.toString();
            String data = JSON.toJSONString(value, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteClassName);
            //value为空时，返回
            if (!StringUtils.hasText(data)) {
                return;
            }
            if (cacheConfig == null) {
                cacheService.set(k, data);
                cacheService.expire(k, expireTime);
            } else {
                // 压缩
                if (!StringUtils.isEmpty(data) && cacheConfig.isUseGzip()) {
                    data = StringSerializeUtil.serializeJSONToGzipHexString(data);
                }
                switch (cacheConfig.getType()) {
                    case CACHE_TYPE_NORMAL:
                        cacheService.set(k, data);
                        break;
                    case CACHE_TYPE_INCR:
                        cacheService.incr(k);
                        break;
                    case CACHE_TYPE_INCR_BY:
                        long incrValue = 0;
                        if (cacheConfig.isNeedExist()) {
                            String orgVal = cacheService.get(k);
                            if (!StringUtils.hasText(orgVal)) {
                                break;
                            }
                        }
                        if (value instanceof Number) {
                            incrValue = NumberUtils.convertNumberToTargetClass((Number) value, Long.class);
                            Long ret = cacheService.incrBy(k, incrValue);
                            if (ret < cacheConfig.getMinValue()) {
                                cacheService.set(k, cacheConfig.getMinValue() + "");
                            }
                        } else {
                            throw new IllegalArgumentException(
                                    "incr by need function return value as a Number type: current"
                                            + value.getClass().getName());
                        }
                        break;
                    case CACHE_TYPE_HASH_ONE:
                        if (cacheConfig.getFields() != null
                                && cacheConfig.getFields().length > 0
                                && StringUtils.hasText(cacheConfig.getFields()[0])) {
                            cacheService.hset(k, cacheConfig.getFields()[0], data);
                        } else {
                            throw new IllegalArgumentException(
                                    "hSet field is null "
                                            + Arrays.toString(cacheConfig.getFields()));
                        }
                        break;
                    case CACHE_TYPE_HASH_INCR:
                        if (cacheConfig.getFields() != null
                                && cacheConfig.getFields().length > 0
                                && StringUtils.hasText(cacheConfig.getFields()[0])) {
                            cacheService.hIncrBy(k, cacheConfig.getFields()[0], 1);
                        } else {
                            throw new IllegalArgumentException(
                                    "hIncrBy field is null "
                                            + Arrays.toString(cacheConfig.getFields()));
                        }
                        break;
                    case CACHE_TYPE_HASH_MULTI:
                        if (cacheConfig.getFields() != null
                                && cacheConfig.getFields().length > 0) {
                            String[] fields = cacheConfig.getFields();
                            String[] cacheFields = cacheConfig.getCacheFields();
                            //put优先使用cacheField，如果没有设置，则采用fields。
                            if (ArrayUtils.isNotEmpty(cacheFields)) {
                                fields = cacheFields;
                            }
                            try {
                                for (String s : fields) {
                                    if (StringUtils.hasText(s)) {
                                        Field field = FieldUtils.getField(value.getClass(), s, true);
                                        if (field != null) {
                                            Object d = field.get(value);
                                            if (d != null) {
                                                String sdata;
                                                Type fieldType = field.getGenericType();
                                                if (((Class) field.getGenericType()).isPrimitive() || String.class.isAssignableFrom((Class<?>) fieldType)) {
                                                    sdata = d.toString();
                                                } else {
                                                    sdata = JSON.toJSONString(d, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteClassName);
                                                }
                                                cacheService.hset(k, s, sdata);
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                logger.error("转换类型错误!", e);
                            }
                        } else {
                            throw new IllegalArgumentException(
                                    "hGet field is null "
                                            + Arrays.toString(cacheConfig.getFields()));
                        }
                        break;
                    case CACHE_TYPE_LIST:
                        throw new IllegalArgumentException(
                                "list cache has not implemented !"
                                        + Arrays.toString(cacheConfig.getFields()));
                    case CACHE_TYPE_SORTED_SET:
                        throw new IllegalArgumentException(
                                "sorted set cache has not implemented !"
                                        + Arrays.toString(cacheConfig.getFields()));
                    default://默认采用普通set
                        cacheService.set(k, data);
                }
                //指定过期时间
                if (cacheConfig.getExpire() > 0) {
                    cacheService.expire(k, cacheConfig.getExpire());
                }
            }
        }
    }

    @Override
    public void evict(Object key) {
        this.evict(null, key);
    }

    @Override
    public void evict(SZCacheConfigBean cacheConfig, Object key) {
        if (key != null) {
            String k = key.toString();
            if (cacheConfig == null) {
                cacheService.delete(k);
            } else {
                switch (cacheConfig.getType()) {
                    case CACHE_TYPE_HASH_ONE://单次操作一个field
                    case CACHE_TYPE_HASH_INCR:
                        if (cacheConfig.getFields() != null
                                && cacheConfig.getFields().length > 0
                                && StringUtils.hasText(cacheConfig.getFields()[0])) {
                            cacheService.hdel(k, cacheConfig.getFields());
                        } else {
                            throw new IllegalArgumentException(
                                    "hGet field is null "
                                            + Arrays.toString(cacheConfig.getFields()));
                        }
                        break;
                    case CACHE_TYPE_HASH_MULTI:
                        if (cacheConfig.getFields() != null
                                && cacheConfig.getFields().length > 0) {
                            String[] fields = cacheConfig.getFields();
                            cacheService.hdel(k, fields);
                        } else {
                            throw new IllegalArgumentException(
                                    "hGet field is null "
                                            + Arrays.toString(cacheConfig.getFields()));
                        }
                        break;
                    case CACHE_TYPE_LIST:
                        throw new IllegalArgumentException(
                                "list cache has not implemented !"
                                        + Arrays.toString(cacheConfig.getFields()));
                    case CACHE_TYPE_SORTED_SET:
                        throw new IllegalArgumentException(
                                "sorted set cache has not implemented !"
                                        + Arrays.toString(cacheConfig.getFields()));
                    default:
                        cacheService.delete(k);
                }
            }
        }
    }

    @Override
    public void clear() {
        this.clear(null);
    }

    /**
     * 暂无实现
     */
    @Override
    public void clear(SZCacheConfigBean cacheConfig) {

    }

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }

    public ICacheService getCacheService() {
        return cacheService;
    }

    public void setCacheService(ICacheService cacheService) {
        this.cacheService = cacheService;
    }
}
