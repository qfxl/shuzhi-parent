package com.shuzhi.cache.test;

import com.shuzhi.cache.annotation.SZCacheConfig;
import com.shuzhi.cache.SZCacheConstant;
import com.shuzhi.cache.SZCacheTypeEnum;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * @author xuyonghong
 * @date 2023-04-10 19:37
 **/
@Service
public class TestService {


    @SZCacheConfig(type = SZCacheTypeEnum.CACHE_TYPE_NORMAL, expire = 60)
    @Cacheable(value = SZCacheConstant.CACHE_DEFAULT_NAME, key = SZCacheConstant.CACHE_GROUP+ "+'-'+#user.userName", condition = "#user.age > 0")
    public User cacheString(User user) {
        return user;
    }

    @SZCacheConfig(type = SZCacheTypeEnum.CACHE_TYPE_INCR, expire = 2 * 60)
    @CachePut(value = SZCacheConstant.CACHE_DEFAULT_NAME, key = "'abc2'+#user.userName", condition = "#user.age > 0")
    public void incrNumber(User user) {

    }

    @SZCacheConfig(type = SZCacheTypeEnum.CACHE_TYPE_NORMAL, expire = 60)
    @Cacheable(value = SZCacheConstant.CACHE_DEFAULT_NAME, key = SZCacheConstant.CACHE_GROUP+ "+'-xuyonghong'")
    public User getUserFromCache() {
        User dbUser = new User();
        dbUser.setUserName("FROM DB");
        return dbUser;
    }

}
