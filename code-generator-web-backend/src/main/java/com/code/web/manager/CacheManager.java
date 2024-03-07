package com.code.web.manager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 多级缓存
 *
 * @author Liang
 * @create 2024/3/4
 */
@Component
public class CacheManager {

    @Resource(name = "myRedisTemplate")
    public RedisTemplate<String, Object> redisTemplate;

    // Caffeine 本地缓存
    Cache<String, Object> localCache = Caffeine.newBuilder()
            .expireAfterWrite(3, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    /**
     * 写入缓存
     * @param key key
     * @param value value
     */
    public void put(String key, Object value) {
        // 写入本地缓存
        localCache.put(key, value);
        // 写入redis分布式缓存
        redisTemplate.opsForValue().set(key, value, 6, TimeUnit.MINUTES);
    }

    /**
     * 读取缓存
     * @param key key
     * @return value
     */
    public Object get(String key) {
        // 从本地缓存获取
        Object value = localCache.getIfPresent(key);
        if (value != null) {
            return value;
        }

        // 本地缓存未命中，从redis中获取
        value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            // 将 redis 数据写入本地缓存
            localCache.put(key, value);
        }
        return value;
    }

    /**
     * 删除缓存
     * @param key key
     */
    public void delete(String key) {
        // 删除本地缓存
        localCache.invalidate(key);
        // 删除redis
        redisTemplate.delete(key);
    }
}
