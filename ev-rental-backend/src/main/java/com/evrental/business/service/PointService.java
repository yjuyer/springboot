package com.evrental.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evrental.business.entity.PointRecord;
import com.evrental.business.mapper.PointRecordMapper;
import com.evrental.common.exception.BusinessException;
import com.evrental.system.entity.SysUser;
import com.evrental.system.mapper.SysUserMapper;
import com.evrental.system.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 积分服务
 * 负责积分记录的增删改查和积分兑换逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRecordMapper pointRecordMapper;
    private final SysUserMapper userMapper;
    private final MemberService memberService;
    private final CouponService couponService;

    /**
     * 添加积分记录并更新用户积分
     */
    @Transactional
    public void addPoints(Long userId, int points, int type, String remark, Long orderId) {
        // 创建积分记录
        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setPoints(points);
        record.setType(type);
        record.setRemark(remark);
        record.setOrderId(orderId);
        pointRecordMapper.insert(record);

        // 更新用户积分
        SysUser user = userMapper.selectById(userId);
        if (user != null) {
            int currentPoints = user.getMemberPoints() != null ? user.getMemberPoints() : 0;
            user.setMemberPoints(currentPoints + points);
            userMapper.updateById(user);
            log.info("积分变动: userId={}, points={}, type={}, totalPoints={}", userId, points, type, currentPoints + points);
        }
    }

    /**
     * 订单完成时自动赠送积分（1元=1积分）
     */
    @Transactional
    public void addOrderPoints(Long userId, java.math.BigDecimal orderAmount, Long orderId) {
        int points = orderAmount.intValue();
        if (points > 0) {
            addPoints(userId, points, 1, "订单完成赠送积分", orderId);
        }
    }

    /**
     * 积分兑换优惠券
     * @param userId 用户ID
     * @param pointCost 消耗积分
     * @param couponId 优惠券ID
     */
    @Transactional
    public void exchangeCoupon(Long userId, int pointCost, Long couponId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        int currentPoints = user.getMemberPoints() != null ? user.getMemberPoints() : 0;
        if (currentPoints < pointCost) {
            throw new BusinessException("积分不足，无法兑换");
        }

        // 扣除积分
        addPoints(userId, -pointCost, 2, "积分兑换优惠券", null);

        // 发放优惠券
        couponService.grantCouponToUser(userId, couponId);

        log.info("积分兑换成功: userId={}, costPoints={}, couponId={}", userId, pointCost, couponId);
    }

    /**
     * 积分兑换免费取消次数
     */
    @Transactional
    public void exchangeFreeCancel(Long userId, int pointCost) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        int currentPoints = user.getMemberPoints() != null ? user.getMemberPoints() : 0;
        if (currentPoints < pointCost) {
            throw new BusinessException("积分不足，无法兑换");
        }

        // 扣除积分
        addPoints(userId, -pointCost, 2, "积分兑换免费取消次数", null);

        // 增加免费取消次数（减少已使用计数）
        int usedCount = user.getFreeCancelCount() != null ? user.getFreeCancelCount() : 0;
        user.setFreeCancelCount(Math.max(0, usedCount - 1));
        userMapper.updateById(user);

        log.info("积分兑换免费取消: userId={}, costPoints={}", userId, pointCost);
    }

    /**
     * 管理员手动调整积分
     */
    @Transactional
    public void adminAdjustPoints(Long userId, int points, String remark) {
        addPoints(userId, points, 4, remark == null ? "管理员调整" : remark, null);
    }

    /**
     * 查询用户的积分记录
     */
    public Map<String, Object> getPointHistory(Long userId, Integer page, Integer size) {
        Page<PointRecord> result = pointRecordMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<PointRecord>()
                        .eq(PointRecord::getUserId, userId)
                        .orderByDesc(PointRecord::getCreateTime));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("pages", result.getPages());
        return data;
    }

    /**
     * 获取用户当前积分
     */
    public int getUserPoints(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) return 0;
        return user.getMemberPoints() != null ? user.getMemberPoints() : 0;
    }
}
