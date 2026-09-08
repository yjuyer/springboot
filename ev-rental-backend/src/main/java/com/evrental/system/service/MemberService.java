package com.evrental.system.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.evrental.system.entity.SysUser;
import com.evrental.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 会员服务
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>计算会员等级 - 根据累计消费金额自动升级</li>
 *   <li>获取会员权益 - 返回当前等级的所有权益</li>
 *   <li>检查免费取消次数 - 判断用户是否还有免费取消次数</li>
 *   <li>应用会员折扣 - 计算会员专属折扣</li>
 * </ul>
 *
 * <p>会员等级（基于累计消费金额）：</p>
 * <ul>
 *   <li>白银会员(0)：0-999元</li>
 *   <li>黄金会员(1)：1000-2999元</li>
 *   <li>白金会员(2)：3000-5999元</li>
 *   <li>钻石会员(3)：6000-9999元</li>
 *   <li>黑金会员(4)：10000元以上</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final SysUserMapper userMapper;

    /** 会员等级定义 */
    private static final Map<Integer, LevelInfo> LEVELS = new HashMap<>();

    static {
        LEVELS.put(0, new LevelInfo(0, "白银会员", "🥈", 0, 0.98, 0));
        LEVELS.put(1, new LevelInfo(1, "黄金会员", "🥇", 1000, 0.95, 1));
        LEVELS.put(2, new LevelInfo(2, "白金会员", "💎", 3000, 0.92, 2));
        LEVELS.put(3, new LevelInfo(3, "钻石会员", "💠", 6000, 0.88, 3));
        LEVELS.put(4, new LevelInfo(4, "黑金会员", "👑", 10000, 0.85, 5));
    }

    /**
     * 根据累计消费计算会员等级
     */
    public int calculateLevel(BigDecimal totalSpent) {
        if (totalSpent == null) return 0;
        double spent = totalSpent.doubleValue();
        if (spent >= 10000) return 4;
        if (spent >= 6000) return 3;
        if (spent >= 3000) return 2;
        if (spent >= 1000) return 1;
        return 0;
    }

    /**
     * 更新用户会员等级
     */
    @Transactional
    public void updateMemberLevel(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) return;

        int newLevel = calculateLevel(user.getTotalSpent());
        if (newLevel != user.getMemberLevel()) {
            user.setMemberLevel(newLevel);
            userMapper.updateById(user);
            log.info("用户会员等级更新: userId={}, newLevel={}", userId, newLevel);
        }
    }

    /**
     * 累加用户消费金额并更新等级
     */
    @Transactional
    public void addSpending(Long userId, BigDecimal amount) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) return;

        BigDecimal newTotal = (user.getTotalSpent() != null ? user.getTotalSpent() : BigDecimal.ZERO).add(amount);
        user.setTotalSpent(newTotal);

        int newLevel = calculateLevel(newTotal);
        user.setMemberLevel(newLevel);

        userMapper.updateById(user);
        log.info("用户消费累加: userId={}, amount={}, totalSpent={}, level={}", userId, amount, newTotal, newLevel);
    }

    /**
     * 完成订单后累加会员积分（1元=1积分，押金不算）
     */
    @Transactional
    public void addMemberPoints(Long userId, BigDecimal orderAmount) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) return;

        // 计算积分（取整，1元=1积分）
        int points = orderAmount.intValue();
        int currentPoints = user.getMemberPoints() != null ? user.getMemberPoints() : 0;
        int newPoints = currentPoints + points;

        user.setMemberPoints(newPoints);
        userMapper.updateById(user);
        log.info("用户积分累加: userId={}, 本次积分={}, 总积分={}", userId, points, newPoints);
    }

    /**
     * 手动设置用户会员等级（管理员使用）
     */
    @Transactional
    public void setMemberLevel(Long userId, int level) {
        if (level < 0 || level > 4) {
            throw new IllegalArgumentException("等级必须在 0-4 之间");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) return;

        user.setMemberLevel(level);
        userMapper.updateById(user);
        log.info("手动设置会员等级: userId={}, level={}", userId, level);
    }

    /**
     * 获取用户会员信息
     */
    public Map<String, Object> getMemberInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) return null;

        int level = user.getMemberLevel() != null ? user.getMemberLevel() : 0;
        LevelInfo levelInfo = LEVELS.get(level);

        Map<String, Object> info = new HashMap<>();
        info.put("level", level);
        info.put("levelName", levelInfo.name);
        info.put("levelIcon", levelInfo.icon);
        info.put("totalSpent", user.getTotalSpent() != null ? user.getTotalSpent() : BigDecimal.ZERO);
        info.put("memberPoints", user.getMemberPoints() != null ? user.getMemberPoints() : 0);
        info.put("freeCancelCount", levelInfo.freeCancelCount);
        info.put("usedCancelCount", user.getFreeCancelCount() != null ? user.getFreeCancelCount() : 0);
        info.put("couponDiscount", levelInfo.couponDiscount);
        info.put("benefits", getBenefits(level));
        return info;
    }

    /**
     * 获取会员权益列表
     */
    public Map<String, Object> getBenefits(int level) {
        Map<String, Object> benefits = new HashMap<>();
        benefits.put("couponDiscount", LEVELS.get(level).couponDiscount);
        benefits.put("birthdayPrivilege", level >= 1);
        benefits.put("freeCancelCount", LEVELS.get(level).freeCancelCount);
        benefits.put("priorityPickup", level >= 2);
        benefits.put("exclusiveService", level >= 3);
        benefits.put("freeUpgrade", level >= 3);
        benefits.put("airportTransfer", level >= 4);
        benefits.put("exclusiveEvent", level >= 4);
        return benefits;
    }

    /**
     * 检查用户是否可以免费取消
     */
    public boolean canFreeCancel(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) return false;

        int level = user.getMemberLevel() != null ? user.getMemberLevel() : 0;
        int maxFreeCancel = LEVELS.get(level).freeCancelCount;
        int usedCount = user.getFreeCancelCount() != null ? user.getFreeCancelCount() : 0;

        return usedCount < maxFreeCancel;
    }

    /**
     * 使用免费取消次数
     */
    @Transactional
    public void useFreeCancel(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) return;

        int usedCount = user.getFreeCancelCount() != null ? user.getFreeCancelCount() : 0;
        user.setFreeCancelCount(usedCount + 1);
        userMapper.updateById(user);
        log.info("用户使用免费取消次数: userId={}, usedCount={}", userId, usedCount + 1);
    }

    /**
     * 重置每月免费取消次数（定时任务调用）
     */
    @Transactional
    public void resetMonthlyFreeCancel() {
        SysUser user = new SysUser();
        user.setFreeCancelCount(0);
        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getFreeCancelCount, 0));
        log.info("重置所有用户免费取消次数");
    }

    /**
     * 获取会员折扣率
     */
    public BigDecimal getMemberDiscount(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) return BigDecimal.ONE;

        int level = user.getMemberLevel() != null ? user.getMemberLevel() : 0;
        return BigDecimal.valueOf(LEVELS.get(level).couponDiscount);
    }

    /**
     * 等级信息内部类
     */
    private static class LevelInfo {
        int level;
        String name;
        String icon;
        int minAmount;
        double couponDiscount;
        int freeCancelCount;

        LevelInfo(int level, String name, String icon, int minAmount, double couponDiscount, int freeCancelCount) {
            this.level = level;
            this.name = name;
            this.icon = icon;
            this.minAmount = minAmount;
            this.couponDiscount = couponDiscount;
            this.freeCancelCount = freeCancelCount;
        }
    }
}
