package com.shuzhi.cache.core.cache;

import com.shuzhi.cache.properties.SZRedisCacheProperties;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

/**
 * Redis缓存服务
 *
 * @author zhangyabo
 */
public class SZRedisCache {
    protected static Logger logger = LoggerFactory.getLogger(SZRedisCache.class);

    @Resource
    private SZRedisCacheProperties cacheProperties;

    private ShardedJedisPool pool;

    /**
     * 初始化Redis连接池
     */
    @PostConstruct
    public void init() {
        Assert.notNull(cacheProperties, "未获取到redis缓存配置，请检查配置文件！");
        GenericObjectPoolConfig<ShardedJedis> config = new GenericObjectPoolConfig<>();
        config.setTestOnBorrow(true);
        config.setMaxIdle(cacheProperties.getPool().getMaxIdle());
        config.setMinIdle(cacheProperties.getPool().getMinIdle());
        config.setMaxTotal(cacheProperties.getPool().getMaxTotal());
        config.setMaxWaitMillis(cacheProperties.getPool().getMaxWaitMillis());

        List<JedisShardInfo> list = new ArrayList<>();
        String hosts = cacheProperties.getHosts();
        String pass = cacheProperties.getPass();
        if (hosts != null && CollectionUtils.isEmpty(list)) {
            String[] hs = hosts.split(",");
            for (String h : hs) {
                if (StringUtils.isNotBlank(h)) {
                    String[] pp = h.split(":");
                    String ip = pp[0];
                    int port = Integer.parseInt(pp[1]);
                    JedisShardInfo jedisShardInfo = new JedisShardInfo(ip, port);
                    if (StringUtils.isNotBlank(pass)) {
                        jedisShardInfo.setPassword(pass);
                    }
                    list.add(jedisShardInfo);
                    logger.info("Redis add={}:{}", ip, port);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(list)) {
            pool = new ShardedJedisPool(config, list);
        } else {
            logger.error("Redis初始化失败，redis.hosts配置无效={}", hosts);
        }
    }

    public void setKeyPrefix(String keyPrefix) {
        cacheProperties.setKeyPrefix(keyPrefix);
    }

    public ShardedJedisPool getShardedJedisPool() {
        return pool;
    }

    /**
     * 获取前缀key
     *
     * @param key
     * @return re 返回redisKey.getKeyPrefix() + key
     */
    public String buildKey(String key) {
        return getKeyPrefix() + key;
    }

    /**
     * 设置hash多个field
     *
     * @param key
     * @param hash
     */
    public void hmset(String key, Map<String, String> hash) {
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            jedis.hmset(k, hash);
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
    }

    /**
     * key是否存在
     *
     * @param key
     * @return
     */
    public boolean exists(String key) {
        boolean re = false;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            re = jedis.exists(k);
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    /**
     * 删除key缓存
     *
     * @param key
     * @return
     */
    public Long remove(String key) {
        Long re = 0L;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);

            re = jedis.del(k);
            if (logger.isDebugEnabled()) {
                logger.debug(k + "-已被删除" + re);
            }
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    public Long removeWithoutPrefix(String key) {
        Long re = 0L;
        try (ShardedJedis jedis = getShardedJedis()) {

            re = jedis.del(key);
            if (logger.isDebugEnabled()) {
                logger.debug(key + "-已被删除" + re);
            }
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    public ShardedJedis getShardedJedis() {
//        logger.warn("current active: {},idle: {},wait: {}",new Object[]{pool.getNumActive(),pool.getNumIdle(),pool.getNumWaiters()});
        return pool.getResource();
    }

    /**
     * (non-Javadoc) 此接口提供给开发人员使用需要再转换为java对象。
     */
    public String get(String rk) {
        String re = null;
        try (ShardedJedis jedis = getShardedJedis()) {
            String key = this.buildKey(rk);
            ;
            if (logger.isDebugEnabled()) {
                logger.debug("get from redisCache :" + key);
            }
            re = jedis.get(key);
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("读取redis服务器失败！", e);
        }
        return re;
    }

    /**
     * hash值的hget
     *
     * @param rk    键
     * @param field 字段名
     * @return re
     */
    public String hget(String rk, String field) {
        String re = null;
        try (ShardedJedis jedis = getShardedJedis()) {
            String key = this.buildKey(rk);
            if (logger.isDebugEnabled()) {
                logger.debug("get from redisCache :" + key + " " + field);
            }
            re = jedis.hget(key, field);
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("读取redis服务器失败！", e);
        }
        return re;
    }

    /**
     * 保存hash字段，会设置该key的expire
     *
     * @param key
     * @param field
     * @param value
     * @return re
     */
    public boolean hset(String key, String field, String value) {
        boolean re = false;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            if (logger.isDebugEnabled()) {
                logger.debug(k + " add to redisCache :{}=" + value, field);
            }
            jedis.hset(k, field, value);
            re = true;
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    /**
     * 保存
     *
     * @param key
     * @param value
     * @return
     */
    public boolean save(String key, String value) {
        boolean re = false;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            if (logger.isDebugEnabled()) {
                logger.debug("{} add to redisCache :{}", key, value);
            }
            jedis.set(k, value);
            re = true;
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }


    public String getKeyPrefix() {
        return cacheProperties.getKeyPrefix();
    }


    public boolean expire(String key, int expiretime) {
        boolean re = false;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            if (logger.isDebugEnabled()) {
                logger.debug("{} expiretime to redisCache :{}", key);
            }
            jedis.expire(k, expiretime);
            if (logger.isDebugEnabled()) {
                logger.debug("缓存超时时间(s):" + expiretime);
            }
            re = true;
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    /**
     * 保存，如果key已经存在将返回0，成功将返回1
     *
     * @param key   键
     * @param value 值
     * @return re 0表示没有插入缓存，1表示已经插入缓存
     * @throws Exception
     */
    public long setnx(String key, String value) {
        long re = 0;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            if (logger.isDebugEnabled()) {
                logger.debug("{} add to redisCache :{}", key, value);
            }
            //jedis.setex(k, this.getExpiretime(), value);
            re = jedis.setnx(k, value);
            //jedis.expire(k, sencond);
            //jedis.set(k, value);
            //jedis.expire(k, redisKey.getExpiretime());
            //logger.debug("缓存超时时间(s):{}", sencond);
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    /**
     * @param key
     * @return
     */
    public long incr(String key) {
        long re = 0;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            if (logger.isDebugEnabled()) {
                logger.debug("{} incr to redisCache :{}", key);
            }
            re = jedis.incr(k);
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    public long incrBy(String key, long by) {
        long re = 0;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            if (logger.isDebugEnabled()) {
                logger.debug("{} incr to redisCache :{}", key);
            }
            re = jedis.incrBy(k, by);
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    /**
     * 获取数组的某一段
     *
     * @param key
     * @param start 从0开始
     * @param end
     * @return
     */
    public List<String> lrange(String key, int start, int end) {
        List<String> re = null;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            re = jedis.lrange(k, start, end);

        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    /**
     * 逐个push元素到列表，并重新设置超时时间
     *
     * @param key
     * @return
     * @throws Exception
     */
    public long lpush(String key, String... values)
            throws Exception {
        long re = 0;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            re = jedis.lpush(k, values);

        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    /**
     * 逐个push元素到列表，并重新设置超时时间
     *
     * @param key
     * @param values 从0开始
     * @return
     */
    public long lpushx(String key, String... values) {
        long re = 0;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            re = jedis.lpushx(k, values);

        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    /**
     * 移除并返回列表key的头元素
     *
     * @param key
     * @return
     */
    public String lpop(String key) {
        String re = null;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            re = jedis.lpop(k);

        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    /**
     * 返回key的数组元素数量
     *
     * @param key
     * @return
     */
    public Long llen(String key) {
        Long re = null;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            re = jedis.llen(k);
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    /**
     * 获取有序集合key的member成员的当前分值
     *
     * @param key    有序集合的key
     * @param member 需要获取的成员member
     * @return re 当前排名，0表示第一名
     * @throws Exception
     */
    public Double zscore(String key, String member) {
        Double re = null;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            re = jedis.zscore(k, member);
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    /**
     * 获取有序集合key的member成员的当前排名
     *
     * @param key    有序集合的key
     * @param member 需要获取的成员member
     * @return re 当前排名，0表示第一名
     * @throws Exception
     */
    public Long zrevrank(String key, String member) {
        Long re = null;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            re = jedis.zrevrank(k, member);

        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    /**
     * 增加集合，不设置过期时间
     *
     * @param key    redis的key
     * @param score  成员的分数
     * @param member 成员member
     * @return
     */
    public Long zadd(String key, int score, String member) {
        Long re = null;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            re = jedis.zadd(k, score, member);
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    /**
     * 增量集合成员的分数，不设置过期时间
     *
     * @param key    redis的key
     * @param score  成员的分数
     * @param member 成员member
     * @return re 当前成员的分数
     */
    public Double zincrby(String key, int score, String member) {
        Double re = null;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            re = jedis.zincrby(k, score, member);
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    /**
     * 获取有序集合key的start、end的闭区间成员
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
        Set<Tuple> re = null;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            re = jedis.zrevrangeWithScores(k, start, end);
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    /**
     * 获取有序集合key的start、end的闭区间成员
     *
     * @param key
     * @return
     */
    public List<String> hmget(String key, String... fields) {
        List<String> re = null;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            ShardedJedisPipeline pipeline = jedis.pipelined();
            Response<List<String>> reList = pipeline.hmget(k, fields);
            pipeline.sync();
            re = reList.get();
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    /**
     * 删除hash指定field
     *
     * @param key
     * @return
     */
    public long hdel(String key, String... fields) {
        long re = 0;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            re = jedis.hdel(k, fields);
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }


    /**
     * 增长hash指定field
     *
     * @param key
     * @return
     */
    public long hIncrBy(String key, String field, int increment) {
        long re = 0;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            re = jedis.hincrBy(k, field, increment);
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    public Set<String> keys(String key) {
        ShardedJedis jedis = null;
        Set<String> re = new HashSet<String>();
//        try {
//            jedis = getShardedJedis();
//            Collection<Jedis> js = jedis.getAllShards();
//            for (Jedis j : js){
//                Client c = j.getClient();
//                //System.out.println(c.getHost());
//                c.keys(key);
//            }
//            String k=this.buildKey(key);
//        }catch (JedisConnectionException e) {
//            logger.error("连接redis服务器失败！",e);
//        }catch (Exception e) {
//            logger.error("redis服务器失败！",e);
//        }finally{
//            if(jedis != null){
//                jedis.close();
//            }
//        }
        return re;
    }

    public List<String> scan(String key) {
        List<String> list = new ArrayList<>();
        try (ShardedJedis jedis = getShardedJedis()) {
            Collection<Jedis> js = jedis.getAllShards();
            for (Jedis j : js) {
                ScanParams params = new ScanParams();
                params.match(key);
                params.count(1000);
                String cursor = "0";
                int cnt = 0;
                while (cnt++ <= 100000) {
                    ScanResult<String> scanResult = j.scan(cursor, params);
                    List<String> elements = scanResult.getResult();
                    if (elements != null && elements.size() > 0) {
                        list.addAll(elements);
                    }
                    cursor = scanResult.getStringCursor();
                    if ("0".equals(cursor)) {
                        break;
                    }
                }
                return list;
            }
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("redis服务器失败！", e);
        }
        return list;
    }

    /**
     * 当 key 存在且是有序集类型时，返回有序集的基数。当 key 不存在时，返回 0 。
     *
     * @param key
     * @return re
     */
    public long zcard(String key) {
        long re = 0;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            re = jedis.zcard(k);
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    public long ttl(String key) {
        long re = 0;
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            re = jedis.ttl(k);
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    public Map<String, String> hGetAll(String key) {
        Map<String, String> re = new LinkedHashMap<String, String>();
        try (ShardedJedis jedis = getShardedJedis()) {
            String k = this.buildKey(key);
            re = jedis.hgetAll(k);
        } catch (JedisConnectionException e) {
            logger.error("连接redis服务器失败！", e);
        } catch (Exception e) {
            logger.error("写入redis服务器失败！", e);
        }
        return re;
    }

    public Map<String, Long> clearCache(List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return null;
        }
        Map<String, Long> cntList = new LinkedHashMap<>(keys.size());
        for (String key : keys) {
            List<String> realKeys = scan(key);
            logger.info(String.valueOf(realKeys));
            Long cnt = 0L;
            if (CollectionUtils.isNotEmpty(realKeys)) {
                for (String rKey : realKeys) {
                    try {
                        cnt += removeWithoutPrefix(rKey);
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }
            }
            cntList.put(key, cnt);
        }
        return cntList;
    }
}
