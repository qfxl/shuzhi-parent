package com.shuzhi.cache.core.service.impl;

import com.shuzhi.cache.core.pojo.CacheTuple;
import com.shuzhi.cache.core.service.ICacheService;
import com.shuzhi.cache.settings.SZCacheSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;


/**
 * 缓存，需要实现：
 * 1、能够切换本地redis（用于单元测试）和线上jimdb
 * 2、支持jone环境下的单元测试：mysql数据库回滚、jimdb单元测试key删除（保存、删除）
 * 环境中如果配置了jimCacheService.cacheType=redis表示将使用redis做为缓存，其余情况将使用jimdb。
 * @author wangxingzhe
 */
//public class CacheService implements ICacheService, ApplicationContextAware {
//    protected static Logger logger = LoggerFactory.getLogger(CacheService.class);
//	@Resource
//	private ICacheService service;
//
//	private ApplicationContext application;
//
//	@Resource
//	private SZCacheSettings szCacheSettings;
//	/**
//	 * 初始化数据源
//	 * 把jimClient设置成延时init，然后根据配置确定要加载哪个数据源
//	 */
//	@PostConstruct
//	public void init(){
////		//根据配置初始化缓存的缓存
////		String cacheType = szCacheSettings.getCacheType();
////		if (SZCacheConstant.CACHE_TYPE_REDIS.equalsIgnoreCase(cacheType)){
////			//需要使用redis的缓存
////			if (application.containsBean("redisCacheService")){
////				this.service = this.application.getBean("redisCacheService", ICacheService.class);
////	            logger.info("采用REDIS缓存！");
////			}else {
////				logger.error("没有发现定义的redisCacheServiceImpl，缓存无法生效");
////			}
////        }
//	}
//
//	@Override
//	public Boolean expire(String key, long second) {
//        if(logger.isDebugEnabled()) {
//			logger.debug("设置缓存过期时间：{},{}", key, second);
//		}
//		return service.expire(key, second);
//	}
//	@Override
//	public void set(String key, String value) {
//        if(logger.isDebugEnabled()) {
//			logger.debug("设置缓存值：{},{}", key, value);
//		}
//		this.service.set(key, value);
//	}
//	@Override
//	public String get(String key) {
//        String ret=this.service.get(key);
//        if(logger.isDebugEnabled()) {
//			logger.debug("获取缓存值：{},{}", key, ret);
//		}
//		return ret;
//	}
//
//	/**
//	 * 计数
//	 * xuchengkuan@jd.com
//	 * 2015年3月26日
//	 * @param key
//	 * @return
//	 */
//	@Override
//	public Long incr(String key){
//        Long ret=this.service.incr(key);
//        if(logger.isDebugEnabled()) {
//			logger.debug("缓存值Incr：{},{}", key, ret);
//		}
//		return ret;
//	}
//	@Override
//	public Long incrBy(String key, Long num){
//        Long ret=this.service.incrBy(key,num);
//        if(logger.isDebugEnabled()) {
//			logger.debug("缓存值IncrBy：{},{}", key, ret);
//		}
//		return ret;
//	}
//	@Override
//	public Long delete(String key){
//        Long ret =this.service.delete(key);
//        if(logger.isDebugEnabled()) {
//			logger.debug("缓存值delete：{},{}", key, ret);
//		}
//		return ret;
//	}
//
//	@Override
//	public void setApplicationContext(ApplicationContext app)
//			throws BeansException {
//		application = app;
//	}
//
//	@Override
//	public boolean setnx(String key, String value) {
//        if(logger.isDebugEnabled()) {
//			logger.debug("缓存值setnx：{},{}", key, value);
//		}
//		return this.setnx(key, value);
//	}
//
//	@Override
//	public Double zscore(String key, String member) {
//        if(logger.isDebugEnabled()) {
//			logger.debug("缓存值zscore：{},{}", key, member);
//		}
//		return service.zscore(key, member);
//	}
//
//	@Override
//	public Long zrevrank(String key, String member) {
//        if(logger.isDebugEnabled()) {
//			logger.debug("缓存值zrevrank：{},{}", key, member);
//		}
//		return service.zrevrank(key, member);
//	}
//
//	@Override
//	public boolean zadd(String key, double score, String member) {
//        if(logger.isDebugEnabled()) {
//			logger.debug("缓存值zadd：{},{},{}", key, score, member);
//		}
//		return service.zadd(key, score, member);
//	}
//
//	@Override
//	public Double zincrby(String key, double score, String member) {
//        if(logger.isDebugEnabled()) {
//			logger.debug("缓存值zadd：{},{},{}", key, score, member);
//		}
//		return service.zincrby(key, score, member);
//	}
//
//	@Override
//	public LinkedHashSet<CacheTuple> zrevrangeWithScores(String key, long start, long end) {
//        if(logger.isDebugEnabled()) {
//			logger.debug("缓存值zrevrangeWithScores：{},{},{}", key, start, end);
//		}
//		return service.zrevrangeWithScores(key, start, end);
//	}
//
//	@Override
//	public String hget(String rk, String field) {
//        String ret=service.hget(rk, field);
//        if(logger.isDebugEnabled()) {
//			logger.debug("获取缓存值hget：{},{},{}", rk, field, ret);
//		}
//		return ret;
//	}
//
//	@Override
//	public boolean hset(String key, String field, String value) {
//        if(logger.isDebugEnabled()) {
//			logger.debug("缓存值hset：{},{},{}", key, field, value);
//		}
//		return service.hset(key, field, value);
//	}
//
//    @Override
//    public long hdel(String key, String... fields) {
//        if(logger.isDebugEnabled()) {
//			logger.debug("获取缓存值hdel：{},{}", key, Arrays.toString(fields));
//		}
//        return service.hdel(key,fields);
//    }
//
//    @Override
//    public long hIncrBy(String key, String field, int increment) {
//        long ret=service.hIncrBy(key,field,increment);
//        if(logger.isDebugEnabled()) {
//			logger.debug("缓存值hIncrBy：{},{},{},{}", key, field, increment, ret);
//		}
//        return ret;
//    }
//
//    @Override
//    public Set<String> keys(String key) {
//        Set<String> keys=service.keys(key);
//        if(logger.isDebugEnabled()) {
//			logger.debug("缓存keys：{},{}", key, keys);
//		}
//        return keys;
//    }
//
//	@Override
//	public List<String> scan(String key) {
//		List<String> keys = service.scan(key);
//		if (logger.isDebugEnabled()) {
//			logger.debug("缓存scan keys：{},{}", key, keys);
//		}
//		return keys;
//	}
//
//	@Override
//    public Long ttl(String key) {
//        return service.ttl(key);
//    }
//
//    @Override
//    public Map<String, String> hGetAll(String key) {
//        return service.hGetAll(key);
//    }
//
//	@Override
//	public List<String> hmget(String rk, String... fields) {
//        List<String> list=service.hmget(rk, fields);
//        if(logger.isDebugEnabled()) {
//			logger.debug("缓存值hmget：{},{},{}", rk, Arrays.toString(fields), list);
//		}
//		return list;
//	}
//
//	@Override
//	public Boolean exists(String key) {
//        Boolean ret=service.exists(key);
//        if(logger.isDebugEnabled()) {
//			logger.debug("缓存值exists：{},{}", key, ret);
//		}
//		return ret;
//	}
//
//    @Override
//    public void setKeyPrefix(String keyPrefix) {
//        this.service.setKeyPrefix(keyPrefix);
//    }
//
//    @Override
//	public void hMSet(String key, Map<String, String> values){
//        if(logger.isDebugEnabled()) {
//			logger.debug("缓存值hMSet：{},{}", key, values);
//		}
//		service.hMSet(key, values);
//	}
//
//	@Override
//	public long zcard(String key) {
//		long re = service.zcard(key);
//		logger.debug("zcard查询{}={}", key, re);
//		return re;
//	}
//
//	@Override
//    public Map<String, Long> clearCache(List<String> keys) {
//		if (logger.isDebugEnabled()) {
//			logger.debug("清空缓存clearCache：{}", keys);
//		}
//		return service.clearCache(keys);
//	}
//}
