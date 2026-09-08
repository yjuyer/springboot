package com.evrental.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 * 封装常用操作：缓存读写、分布式锁
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    /** 设置缓存 */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /** 设置缓存并指定过期时间 */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /** 获取缓存 */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /** 删除缓存 */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /** 判断key是否存在 */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 分布式锁 - 尝试获取锁
     * @param key 锁的key
     * @param timeout 超时时间
     * @param unit 时间单位
     */
    public boolean tryLock(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, "1", timeout, unit));
    }

    /** 分布式锁 - 释放锁 */
    public void unlock(String key) {
        delete(key);
    }
}
