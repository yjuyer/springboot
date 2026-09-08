package com.evrental.business.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.evrental.business.entity.ActivityParticipation;
import com.evrental.business.entity.PromotionActivity;
import com.evrental.business.mapper.ActivityParticipationMapper;
import com.evrental.business.mapper.PromotionActivityMapper;
import com.evrental.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 优惠活动引擎服务
 * 负责促销活动管理和下单时的折扣计算
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionActivityMapper activityMapper;
    private final ActivityParticipationMapper participationMapper;

    // ==================== 管理端 CRUD ====================

    /**
     * 创建促销活动
     */
    @Transactional
    public void createActivity(PromotionActivity activity) {
        activity.setUsedCount(0);
        activityMapper.insert(activity);
        log.info("促销活动创建成功: {}", activity.getActivityName());
    }

    /**
     * 更新促销活动
     */
    @Transactional
    public void updateActivity(PromotionActivity activity) {
        PromotionActivity exist = activityMapper.selectById(activity.getId());
        if (exist == null) {
            throw new BusinessException("活动不存在");
        }
        activityMapper.updateById(activity);
        log.info("促销活动更新成功: id={}", activity.getId());
    }

    /**
     * 删除促销活动
     */
    @Transactional
    public void deleteActivity(Long id) {
        activityMapper.deleteById(id);
        log.info("促销活动已删除: id={}", id);
    }

    /**
     * 启用/禁用活动
     */
    @Transactional
    public void toggleActivity(Long id) {
        PromotionActivity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        activity.setStatus(activity.getStatus() == 1 ? 0 : 1);
        activityMapper.updateById(activity);
        log.info("活动状态已切换: id={}, status={}", id, activity.getStatus());
    }

    /**
     * 分页查询活动列表
     */
    public Map<String, Object> listActivities(Integer page, Integer size, Integer status) {
        LambdaQueryWrapper<PromotionActivity> qw = new LambdaQueryWrapper<PromotionActivity>()
                .orderByDesc(PromotionActivity::getCreateTime);

        if (status != null) {
            qw.eq(PromotionActivity::getStatus, status);
        }

        Page<PromotionActivity> result = activityMapper.selectPage(new Page<>(page, size), qw);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("pages", result.getPages());
        return data;
    }

    // ==================== 折扣计算引擎 ====================

    /**
     * 获取当前有效的所有活动
     */
    public List<PromotionActivity> getActiveActivities() {
        LocalDateTime now = LocalDateTime.now();
        return activityMapper.selectList(new LambdaQueryWrapper<PromotionActivity>()
                .eq(PromotionActivity::getStatus, 1)
                .le(PromotionActivity::getStartTime, now)
                .ge(PromotionActivity::getEndTime, now));
    }

    /**
     * 计算活动最优折扣
     * @param userId 用户ID
     * @param orderAmount 订单金额（租金）
     * @return 最优活动及其优惠金额，无可用活动返回null
     */
    public ActivityDiscountResult calculateBestDiscount(Long userId, BigDecimal orderAmount) {
        List<PromotionActivity> activities = getActiveActivities();
        if (activities.isEmpty()) {
            return null;
        }

        PromotionActivity bestActivity = null;
        BigDecimal bestDiscount = BigDecimal.ZERO;

        for (PromotionActivity activity : activities) {
            // 检查用户参与次数限制
            if (activity.getUserLimit() > 0) {
                Long count = participationMapper.selectCount(new LambdaQueryWrapper<ActivityParticipation>()
                        .eq(ActivityParticipation::getActivityId, activity.getId())
                        .eq(ActivityParticipation::getUserId, userId));
                if (count >= activity.getUserLimit()) {
                    continue;
                }
            }

            // 检查总参与次数限制
            if (activity.getTotalLimit() > 0 && activity.getUsedCount() >= activity.getTotalLimit()) {
                continue;
            }

            // 计算折扣金额
            BigDecimal discount = calculateActivityDiscount(activity, orderAmount);
            if (discount.compareTo(bestDiscount) > 0) {
                bestDiscount = discount;
                bestActivity = activity;
            }
        }

        if (bestActivity == null || bestDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return new ActivityDiscountResult(bestActivity, bestDiscount);
    }

    /**
     * 计算单个活动的折扣金额
     */
    private BigDecimal calculateActivityDiscount(PromotionActivity activity, BigDecimal orderAmount) {
        JSONObject rule = JSON.parseObject(activity.getRuleConfig());
        BigDecimal minAmount = rule.getBigDecimal("minAmount");

        // 不满足最低消费
        if (minAmount != null && orderAmount.compareTo(minAmount) < 0) {
            return BigDecimal.ZERO;
        }

        int type = activity.getActivityType();
        switch (type) {
            case 1: // 满减
                BigDecimal discountAmount = rule.getBigDecimal("discountAmount");
                return discountAmount != null ? discountAmount : BigDecimal.ZERO;

            case 2: // 折扣
                BigDecimal discountRate = rule.getBigDecimal("discountRate");
                if (discountRate != null) {
                    return orderAmount.multiply(BigDecimal.ONE.subtract(discountRate))
                            .setScale(2, RoundingMode.HALF_UP);
                }
                return BigDecimal.ZERO;

            case 3: // 立减
                BigDecimal directAmount = rule.getBigDecimal("discountAmount");
                return directAmount != null ? directAmount : BigDecimal.ZERO;

            default:
                return BigDecimal.ZERO;
        }
    }

    /**
     * 计算活动+优惠券叠加后的最终优惠
     * @param activityDiscount 活动优惠金额（可能为null）
     * @param couponDiscount 优惠券优惠金额（可能为null）
     * @param orderAmount 订单原金额
     * @param activity 活动（用于判断是否可叠加和上限）
     * @return 最终优惠金额
     */
    public BigDecimal calculateCombinedDiscount(BigDecimal activityDiscount, BigDecimal couponDiscount,
                                                 BigDecimal orderAmount, PromotionActivity activity) {
        if (activityDiscount == null) activityDiscount = BigDecimal.ZERO;
        if (couponDiscount == null) couponDiscount = BigDecimal.ZERO;

        BigDecimal totalDiscount;
        if (activity != null && activity.getStackable() == 1) {
            // 可叠加：活动 + 优惠券
            totalDiscount = activityDiscount.add(couponDiscount);
            // 检查叠加上限
            BigDecimal maxDiscount = activity.getMaxDiscount();
            if (maxDiscount != null && maxDiscount.compareTo(BigDecimal.ZERO) > 0
                    && totalDiscount.compareTo(maxDiscount) > 0) {
                totalDiscount = maxDiscount;
            }
        } else {
            // 不可叠加：取最大折扣
            totalDiscount = activityDiscount.max(couponDiscount);
        }

        // 折扣不能超过订单金额
        if (totalDiscount.compareTo(orderAmount) > 0) {
            totalDiscount = orderAmount;
        }

        return totalDiscount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 记录活动参与
     */
    @Transactional
    public void recordParticipation(Long activityId, Long userId, Long orderId, String orderNo, BigDecimal discountAmount) {
        ActivityParticipation part = new ActivityParticipation();
        part.setActivityId(activityId);
        part.setUserId(userId);
        part.setOrderId(orderId);
        part.setOrderNo(orderNo);
        part.setDiscountAmount(discountAmount);
        part.setCreateTime(LocalDateTime.now());
        participationMapper.insert(part);

        // 更新活动参与计数
        activityMapper.update(null, new LambdaUpdateWrapper<PromotionActivity>()
                .eq(PromotionActivity::getId, activityId)
                .setSql("used_count = used_count + 1"));

        log.info("活动参与记录: activityId={}, userId={}, orderId={}, discount={}",
                activityId, userId, orderId, discountAmount);
    }

    /**
     * 活动折扣计算结果
     */
    public static class ActivityDiscountResult {
        private final PromotionActivity activity;
        private final BigDecimal discountAmount;

        public ActivityDiscountResult(PromotionActivity activity, BigDecimal discountAmount) {
            this.activity = activity;
            this.discountAmount = discountAmount;
        }

        public PromotionActivity getActivity() { return activity; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
    }
}
