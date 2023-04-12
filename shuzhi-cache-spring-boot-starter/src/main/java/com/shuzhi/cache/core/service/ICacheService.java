package com.shuzhi.cache.core.service;

import com.shuzhi.cache.core.pojo.CacheTuple;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用于jimdb的缓存实现
 * @author wangxingzhe 2016-5-26 21:47:12
 */
public interface ICacheService {
	/**
	 * 设置key prefix
	 * @param keyPrefix
	 */
    void setKeyPrefix(String keyPrefix);
	/**
	 * 设置hash的多个值
	 * @param key 
	 * @param values 
	 */
	void hMSet(String key, Map<String, String> values);
	
	/**
	 * 指定的key是否存在，true表示存在
	 * @param key 
	 * @return re true表示存在
	 */
	Boolean exists(String key);
	/**
	 * 指定key的过期时间
	 * @param key key键
	 * @param second 过期时间秒数
	 * @return re 
	 */
	Boolean expire(String key, long second);
	/**
	 * 保存数据
	 * @param key
	 * @param value
	 */
	void set(String key, String value);
	/**
	 * 获取key的数据
	 * @param key 键
	 * @return re 值
	 */
	String get(String key);
	/**
	 * 计数
	 * xuchengkuan@jd.com
	 * 2015年3月26日
	 * @param key
	 * @return
	 */
	Long incr(String key);

	/**
	 * 步进
	 * @param key
	 * @param num
	 * @return
	 */
	Long incrBy(String key, Long num);

	/**
	 * 删除
	 * @param key
	 * @return
	 */
	Long delete(String key);
	/**
	 * 保存，如果key已经存在将返回0，成功将返回true
	 * @param key 键
	 * @param value 值
	 * @return re false表示没有插入缓存，true表示已经插入缓存
	 */
	boolean setnx(String key, String value);
	/**
	 * 获取有序集合key的member成员的当前分值
	 * @param key 有序集合的key
	 * @param member 需要获取的成员member
	 * @return re 当前分数
	 */
	Double zscore(String key, String member);
	/**
	 * 获取有序集合key的member成员的当前排名
	 * @param key 有序集合的key
	 * @param member 需要获取的成员member
	 * @return re 当前排名，0表示第一名
	 */
	Long zrevrank(String key, String member);
	/**
     * 增加集合成员
     * @param key redis的key
     * @param score 成员的分数
     * @param member 成员member
     * @return 
     */
	boolean zadd(String key, double score, String member);
	
	/**
     * 增量集合成员的分数，不设置过期时间
     * @param key redis的key
     * @param score 成员的分数
     * @param member 成员member
     * @return re 当前成员的分数
     */
	Double zincrby(String key, double score, String member);
    
    /**
     * 获取有序集合key的start、end的闭区间成员。
     * 其中成员的位置按 score 值递减(从大到小)来排列。
     * @param key 
     * @param start 
     * @param end 
     * @return 
     */
	LinkedHashSet<CacheTuple> zrevrangeWithScores(String key, long start, long end);
    
    /**
     * 当 key存在且是有序集类型时，返回有序集的基数。当 key 不存在时，返回 0
     * @param key ken值
     * @return re 当 key存在且是有序集类型时，返回有序集的基数。当 key 不存在时，返回 0 。
     */
	long zcard(String key);
    
    /**
     * hash值的hget
     * @param rk 键
     * @param field 字段名
     * @return
     */
	String hget(String rk, String field);
    /**
     * hash值的hget，获取多个字段
     * @param rk key值
     * @param fields 字段，可放入多个
     * @return re 
     */
	List<String> hmget(String rk, String... fields);
    
    /**
     * 保存hash字段，
     * @param key 键
     * @param field 字段名
     * @param value 值
     * @return re 
     */
	boolean hset(String key, String field, String value);

    /**
     * 删除hash字段
     * @param key 键
     * @param fields 字段名
     * @return re
     */
	long hdel(String key, String... fields);

    /**
     * 增长hash字段
     * @param key 键
     * @param field 字段名
     * @return re
     */
	long hIncrBy(String key, String field, int increment);


    /**
     * 批量获取key
     * @param key
     * @return
     */
	Set<String> keys(String key);

	/**
	 * 批量获取key
	 *
	 * @param key
	 * @return
	 */
	List<String> scan(String key);

    /**
     * 查询剩余时间
     * @param key
     * @return
     */
	Long ttl(String key);

    /**
     * 查询hgetall
     * @param key
     * @return
     */
	Map<String,String>  hGetAll(String key);

	/**
	 * 清空缓存
	 *
	 * @param keys
	 * @return
	 */
    Map<String, Long> clearCache(List<String> keys);
}
