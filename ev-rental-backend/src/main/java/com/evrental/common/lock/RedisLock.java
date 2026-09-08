package com.evrental.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类
 *
 * 设计思路：
 * 1. 使用 SET key value NX EX 实现加锁（原子操作）
 * 2. 使用 Lua 脚本实现解锁（判断value再删除，防止误删别人的锁）
 * 3. 每个锁持有者生成唯一UUID，确保只能释放自己的锁
 * 4. 支持可重入（通过计数器实现，本方案简化为不可重入）
 *
 * Redis Key 设计：
 * ┌─────────────────────────────────────────────────────────┐
 * │  Key格式                        │  含义                 │
 * ├─────────────────────────────────────────────────────────┤
 * │  lock:rental:vehicle:{vehicleId} │  车辆租赁锁          │
 * │  lock:order:user:{userId}        │  用户防重复下单锁    │
 * │  lock:stock:vehicle:{vehicleId}  │  车辆库存扣减锁      │
 * └─────────────────────────────────────────────────────────┘
 *
 * @author ev-rental
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLock {

    private final StringRedisTemplate redisTemplate;

    // ======================== Lua脚本 ========================

    /**
     * 解锁Lua脚本
     * 原子性：先比较value是否是自己的锁，再删除
     * 防止误删其他线程持有的锁
     *
     * KEYS[1] = 锁的key
     * ARGV[1] = 当前线程的UUID值
     * 返回值: 1=释放成功, 0=锁不属于当前线程
     */
    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    /**
     * 续期Lua脚本（看门狗机制简化版）
     * KEYS[1] = 锁的key
     * ARGV[1] = 当前线程的UUID值
     * ARGV[2] = 新的过期时间（秒）
     * 返回值: 1=续期成功, 0=锁不属于当前线程
     */
    private static final String RENEW_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('expire', KEYS[1], ARGV[2]) " +
            "else " +
            "    return 0 " +
            "end";

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT_OBJ = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
    private static final DefaultRedisScript<Long> RENEW_SCRIPT_OBJ = new DefaultRedisScript<>(RENEW_SCRIPT, Long.class);

    // ======================== 核心方法 ========================

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey    锁的key
     * @param leaseTime  锁持有时间（秒）
     * @param timeUnit   时间单位
     * @return lockValue 如果获取成功返回唯一标识，获取失败返回null
     */
    public String tryLock(String lockKey, long leaseTime, TimeUnit timeUnit) {
        // 生成唯一锁标识（线程ID + UUID，保证集群环境唯一）
        String lockValue = Thread.currentThread().getId() + ":" + UUID.randomUUID().toString().replace("-", "");

        long seconds = timeUnit.toSeconds(leaseTime);

        // SET key value NX EX seconds —— 原子操作
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, seconds, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(success)) {
            log.debug("获取锁成功: key={}, value={}", lockKey, lockValue);
            return lockValue;
        }

        log.debug("获取锁失败: key={}", lockKey);
        return null;
    }

    /**
     * 释放分布式锁（使用Lua脚本保证原子性）
     *
     * @param lockKey   锁的key
     * @param lockValue 加锁时返回的唯一标识
     * @return 是否释放成功
     */
    public boolean unlock(String lockKey, String lockValue) {
        Long result = redisTemplate.execute(
                UNLOCK_SCRIPT_OBJ,
                Collections.singletonList(lockKey),
                lockValue
        );

        boolean success = result != null && result > 0;
        log.debug("释放锁{}: key={}, value={}", success ? "成功" : "失败", lockKey, lockValue);
        return success;
    }

    /**
     * 续期锁（延长锁的过期时间）
     * 适用于业务执行时间可能超过初始锁时间的场景
     */
    public boolean renewLock(String lockKey, String lockValue, long leaseTime, TimeUnit timeUnit) {
        long seconds = timeUnit.toSeconds(leaseTime);
        Long result = redisTemplate.execute(
                RENEW_SCRIPT_OBJ,
                Collections.singletonList(lockKey),
                lockValue,
                String.valueOf(seconds)
        );
        return result != null && result > 0;
    }

    /**
     * 快速加锁并执行业务（推荐方式）
     *
     * @param lockKey     锁key
     * @param leaseTime   锁时间
     * @param timeUnit    时间单位
     * @param business    业务逻辑
     * @return 业务返回值
     */
    public <T> T executeWithLock(String lockKey, long leaseTime, TimeUnit timeUnit, LockCallback<T> business) {
        String lockValue = tryLock(lockKey, leaseTime, timeUnit);
        if (lockValue == null) {
            throw new RuntimeException("系统繁忙，请稍后重试");
        }
        try {
            return business.doInLock();
        } finally {
            unlock(lockKey, lockValue);
        }
    }

    /** 业务回调接口 */
    @FunctionalInterface
    public interface LockCallback<T> {
        T doInLock();
    }
}
