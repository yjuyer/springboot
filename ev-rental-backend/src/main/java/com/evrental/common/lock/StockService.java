package com.evrental.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Redis车辆库存管理（防超卖核心）
 *
 * 核心思想：把车辆"可租赁"状态当作库存，用Redis原子操作保证不超卖
 *
 * ┌──────────────────────────────────────────────────────────────────┐
 * │                    车辆库存状态设计                                │
 * ├──────────────────────────────────────────────────────────────────┤
 * │  stock:vehicle:{id}     =  1 表示可租 / 0 表示不可租             │
 * │  status:vehicle:{id}    =  空闲/已预约/租赁中/维修中/充电中       │
 * │                                                                  │
 * │  扣库存 = 将stock从1改为0（原子操作）                             │
 * │  还库存 = 将stock从0改为1（还车/取消时恢复）                      │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * @author ev-rental
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockService {

    private final StringRedisTemplate redisTemplate;

    // ======================== Key前缀 ========================
    private static final String STOCK_KEY_PREFIX = "stock:vehicle:";
    private static final String STATUS_KEY_PREFIX = "status:vehicle:";
    private static final String RENTAL_LOCK_PREFIX = "lock:rental:vehicle:";
    private static final String ORDER_LOCK_PREFIX = "lock:order:user:";

    // ======================== Lua脚本 ========================

    /**
     * 扣库存Lua脚本（原子性判断+扣减）
     *
     * 逻辑：
     * 1. 检查库存是否为1（可租）
     * 2. 如果可租，设置为0（已租出）
     * 3. 如果不可租，返回0
     *
     * KEYS[1] = 车辆库存key
     * 返回值: 1=扣减成功, 0=库存不足
     */
    private static final String DEDUCT_STOCK_SCRIPT =
            "local stock = redis.call('get', KEYS[1]) " +
            "if stock == '1' then " +
            "    redis.call('set', KEYS[1], '0') " +
            "    return 1 " +
            "else " +
            "    return 0 " +
            "end";

    /**
     * 还库存Lua脚本
     *
     * KEYS[1] = 车辆库存key
     * 返回值: 1=归还成功, 0=已经是可租状态（重复归还）
     */
    private static final String RESTORE_STOCK_SCRIPT =
            "local stock = redis.call('get', KEYS[1]) " +
            "if stock == '0' then " +
            "    redis.call('set', KEYS[1], '1') " +
            "    return 1 " +
            "else " +
            "    return 0 " +
            "end";

    /**
     * 检查并扣减库存（带状态检查）
     *
     * KEYS[1] = 车辆库存key
     * KEYS[2] = 车辆状态key
     * ARGV[1] = 期望的状态值（如"IDLE"表示空闲）
     * 返回值: 1=成功, 0=库存不足, -1=状态不匹配
     */
    private static final String CHECK_AND_DEDUCT_SCRIPT =
            "local stock = redis.call('get', KEYS[1]) " +
            "if stock ~= '1' then " +
            "    return 0 " +  // 库存不足
            "end " +
            "local status = redis.call('get', KEYS[2]) " +
            "if status ~= ARGV[1] then " +
            "    return -1 " + // 状态不匹配
            "end " +
            "redis.call('set', KEYS[1], '0') " +
            "redis.call('set', KEYS[2], 'RESERVED') " +
            "return 1";

    // 编译后的脚本对象
    private static final DefaultRedisScript<Long> DEDUCT_SCRIPT_OBJ =
            new DefaultRedisScript<>(DEDUCT_STOCK_SCRIPT, Long.class);
    private static final DefaultRedisScript<Long> RESTORE_SCRIPT_OBJ =
            new DefaultRedisScript<>(RESTORE_STOCK_SCRIPT, Long.class);
    private static final DefaultRedisScript<Long> CHECK_DEDUCT_SCRIPT_OBJ =
            new DefaultRedisScript<>(CHECK_AND_DEDUCT_SCRIPT, Long.class);

    // ======================== 对外方法 ========================

    /**
     * 初始化车辆库存到Redis
     * 启动时或车辆上线时调用
     */
    public void initStock(Long vehicleId, boolean available) {
        String stockKey = STOCK_KEY_PREFIX + vehicleId;
        String statusKey = STATUS_KEY_PREFIX + vehicleId;
        redisTemplate.opsForValue().set(stockKey, available ? "1" : "0");
        redisTemplate.opsForValue().set(statusKey, "IDLE");
        log.info("初始化车辆库存: vehicleId={}, available={}", vehicleId, available);
    }

    /**
     * 扣减库存（简单版：只检查库存）
     *
     * @param vehicleId 车辆ID
     * @return true=扣减成功（可以租赁）, false=库存不足（已被租出）
     */
    public boolean deductStock(Long vehicleId) {
        String stockKey = STOCK_KEY_PREFIX + vehicleId;
        Long result = redisTemplate.execute(
                DEDUCT_SCRIPT_OBJ,
                Collections.singletonList(stockKey)
        );
        boolean success = result != null && result > 0;
        log.info("扣减库存{}: vehicleId={}", success ? "成功" : "失败", vehicleId);
        return success;
    }

    /**
     * 检查并扣减库存（推荐：同时检查库存和状态）
     *
     * @param vehicleId    车辆ID
     * @param expectStatus 期望的车辆状态（如"IDLE"）
     * @return 1=成功, 0=库存不足, -1=状态不匹配
     */
    public int checkAndDeductStock(Long vehicleId, String expectStatus) {
        String stockKey = STOCK_KEY_PREFIX + vehicleId;
        String statusKey = STATUS_KEY_PREFIX + vehicleId;

        Long result = redisTemplate.execute(
                CHECK_DEDUCT_SCRIPT_OBJ,
                Arrays.asList(stockKey, statusKey),
                expectStatus
        );

        int code = result != null ? result.intValue() : 0;
        log.info("检查扣减库存: vehicleId={}, expectStatus={}, result={}", vehicleId, expectStatus, code);
        return code;
    }

    /**
     * 恢复库存（还车/取消订单时调用）
     *
     * @param vehicleId 车辆ID
     * @return true=恢复成功
     */
    public boolean restoreStock(Long vehicleId) {
        String stockKey = STOCK_KEY_PREFIX + vehicleId;
        String statusKey = STATUS_KEY_PREFIX + vehicleId;

        Long result = redisTemplate.execute(
                RESTORE_SCRIPT_OBJ,
                Collections.singletonList(stockKey)
        );

        if (result != null && result > 0) {
            redisTemplate.opsForValue().set(statusKey, "IDLE");
            log.info("恢复库存成功: vehicleId={}", vehicleId);
            return true;
        }
        log.warn("恢复库存失败: vehicleId={}", vehicleId);
        return false;
    }

    /** 检查车辆是否可租 */
    public boolean isAvailable(Long vehicleId) {
        String stockKey = STOCK_KEY_PREFIX + vehicleId;
        String val = redisTemplate.opsForValue().get(stockKey);
        return "1".equals(val);
    }

    /** 获取租赁锁Key */
    public String getRentalLockKey(Long vehicleId) {
        return RENTAL_LOCK_PREFIX + vehicleId;
    }

    /** 获取用户订单锁Key */
    public String getOrderLockKey(Long userId) {
        return ORDER_LOCK_PREFIX + userId;
    }
}
