package com.shuzhi.cache.core.service.impl;

import com.shuzhi.cache.core.cache.SZRedisCache;
import com.shuzhi.cache.core.pojo.CacheTuple;
import com.shuzhi.cache.core.service.ICacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Tuple;

import javax.annotation.Resource;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * redis缓存实现
 *
 * @author wangxingzhe
 */
public class RedisCacheServiceImpl implements ICacheService {
    protected static Logger logger = LoggerFactory.getLogger(RedisCacheServiceImpl.class);

    @Resource
    private SZRedisCache redisCache;

    @Override
    public Boolean expire(String key, long second) {
        return redisCache.expire(key, (int) second);
    }

    @Override
    public void set(String key, String value) {
        try {
            redisCache.save(key, value);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    @Override
    public String get(String key) {
        String re = null;
        try {
            re = redisCache.get(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return re;
    }

    @Override
    public Long incr(String key) {
        return redisCache.incr(key);
    }

    @Override
    public Long incrBy(String key, Long num) {
        return redisCache.incrBy(key, num);
    }

    @Override
    public Long delete(String key) {
        long re = 0;
        try {
            re = redisCache.remove(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return re;
    }

    @Override
    public boolean setnx(String key, String value) {
        long ret = redisCache.setnx(key, value);
        return ret > 0;
    }

    @Override
    public Double zscore(String key, String member) {

        return redisCache.zscore(key, member);
    }

    @Override
    public Long zrevrank(String key, String member) {

        return redisCache.zrevrank(key, member);
    }

    @Override
    public boolean zadd(String key, double score, String member) {
        long ret = redisCache.zadd(key, Double.valueOf(score).intValue(), member);
        return ret > 0;
    }

    @Override
    public Double zincrby(String key, double score, String member) {

        return redisCache.zincrby(key, Double.valueOf(score).intValue(), member);
    }

    @Override
    public LinkedHashSet<CacheTuple> zrevrangeWithScores(String key, long start, long end) {
        LinkedHashSet<CacheTuple> re = null;
        Set<Tuple> ret = redisCache.zrevrangeWithScores(key, start, end);
        logger.info("{}", ret);
        if (ret != null) {
            re = new LinkedHashSet<CacheTuple>();
            for (Tuple t : ret) {
                CacheTuple s = new CacheTuple(t.getElement(), t.getScore());
                re.add(s);
            }
        }
        return re;
    }

    @Override
    public String hget(String rk, String field) {

        return redisCache.hget(rk, field);
    }

    @Override
    public boolean hset(String key, String field, String value) {

        return redisCache.hset(key, field, value);
    }

    @Override
    public long hdel(String key, String... fields) {
        return redisCache.hdel(key, fields);
    }

    @Override
    public long hIncrBy(String key, String field, int increment) {
        return redisCache.hIncrBy(key, field, increment);
    }

    @Override
    public Set<String> keys(String key) {
        return redisCache.keys(key);
    }

    @Override
    public List<String> scan(String key) {
        return redisCache.scan(key);
    }

    @Override
    public Long ttl(String key) {
        return redisCache.ttl(key);
    }

    @Override
    public Map<String, String> hGetAll(String key) {
        return redisCache.hGetAll(key);
    }

    @Override
    public List<String> hmget(String rk, String... fields) {

        return redisCache.hmget(rk, fields);
    }

    @Override
    public Boolean exists(String key) {

        return redisCache.exists(key);
    }

    @Override
    public void setKeyPrefix(String keyPrefix) {
        this.redisCache.setKeyPrefix(keyPrefix);
    }

    @Override
    public void hMSet(String key, Map<String, String> values) {
        if (key != null && values != null && !values.isEmpty()) {
            redisCache.hmset(key, values);
        }
    }

    @Override
    public long zcard(String key) {

        return redisCache.zcard(key);
    }

    @Override
    public Map<String, Long> clearCache(List<String> keys) {
        return redisCache.clearCache(keys);
    }

}
