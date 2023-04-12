package com.shuzhi.cache.core.pojo;

import com.shuzhi.cache.SZCacheTypeEnum;
import org.codehaus.jackson.type.TypeReference;

import java.lang.reflect.Type;

/**
 * 配置bean，用于传递给执行缓存操作的support类
 * Created by wangxingzhe on 2015/4/4.
 */
public class SZCacheConfigBean {
    public static final TypeReference<Object> DEFAULT_TYPE_REFERENCE = new TypeReference<Object>() {
    };
    /**
     * 类型
     *
     * @return
     */
    private SZCacheTypeEnum type = SZCacheTypeEnum.CACHE_TYPE_NORMAL;

    /**
     * hash模式下，字段名称
     *
     * @return
     */
    private String[] fields;

    /**
     * hash模式下，缓存字段名称，如果此项为空，则仅采用上面的fields配置
     *
     * @return
     */
    private String[] cacheFields;

    /**
     * 超时时间
     *
     * @return
     */
    private int expire = 86400;

    private Type returnType;

    /**
     * 是否key必须存在
     */
    private boolean needExist = false;

    private long minValue;

    private boolean useGzip;

    /**
     * 是否缓存空数据
     */
    private boolean forceCacheEmpty;

    public SZCacheTypeEnum getType() {
        return type;
    }

    public void setType(SZCacheTypeEnum type) {
        this.type = type;
    }

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    public String[] getCacheFields() {
        return cacheFields;
    }

    public void setCacheFields(String[] cacheFields) {
        this.cacheFields = cacheFields;
    }

    public void setFields(int index, String value) {
        if (this.fields != null && index < this.fields.length) {
            this.fields[index] = value;
        }
    }

    public int getExpire() {
        return expire;
    }

    public void setExpire(int expire) {
        this.expire = expire;
    }

    public Type getReturnType() {
        return returnType;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public boolean isNeedExist() {
        return needExist;
    }

    public void setNeedExist(boolean needExist) {
        this.needExist = needExist;
    }

    public long getMinValue() {
        return minValue;
    }

    public void setMinValue(long minValue) {
        this.minValue = minValue;
    }

    public boolean isUseGzip() {
        return useGzip;
    }

    public void setUseGzip(boolean useGzip) {
        this.useGzip = useGzip;
    }

    public boolean isForceCacheEmpty() {
        return forceCacheEmpty;
    }

    public void setForceCacheEmpty(boolean forceCacheEmpty) {
        this.forceCacheEmpty = forceCacheEmpty;
    }
}
