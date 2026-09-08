package com.evrental.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evrental.business.entity.Coupon;
import com.evrental.business.entity.UserCoupon;
import com.evrental.business.mapper.CouponMapper;
import com.evrental.business.mapper.UserCouponMapper;
import com.evrental.business.service.CouponService;
import com.evrental.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 优惠券服务实现类
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>获取可领取优惠券列表 - 过滤已领完、已过期、用户已领取的优惠券</li>
 *   <li>领取优惠券 - 校验有效性、防止重复领取、更新领取数量</li>
 *   <li>获取用户优惠券 - 查询用户所有优惠券（未使用/已使用/已过期）</li>
 *   <li>计算折扣 - 根据优惠券类型计算优惠金额（满减/折扣/立减）</li>
 *   <li>使用优惠券 - 标记为已使用并关联订单</li>
 * </ul>
 *
 * <p>优惠券类型：</p>
 * <ul>
 *   <li>类型1 - 满减券：满足最低消费后减免固定金额</li>
 *   <li>类型2 - 折扣券：按比例打折（如8折 = 0.8）</li>
 *   <li>类型3 - 立减券：直接减免固定金额</li>
 * </ul>
 *
 * @author ev-rental-team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon>
        implements CouponService {

    private final UserCouponMapper userCouponMapper;

    @Override
    public List<Coupon> getAvailableCoupons(Long userId) {
        // 1. 查询所有有效优惠券
        List<Coupon> allCoupons = list(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getStatus, 1)
                .gt(Coupon::getEndTime, LocalDateTime.now())
                .apply("(total_count = 0 OR total_count > used_count)")
                .orderByDesc(Coupon::getDiscountValue));

        if (userId == null) {
            return allCoupons;
        }

        // 2. 查询用户已领取的优惠券ID
        List<UserCoupon> userCoupons = userCouponMapper.selectList(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .select(UserCoupon::getCouponId));
        Set<Long> claimedCouponIds = userCoupons.stream()
                .map(UserCoupon::getCouponId)
                .collect(Collectors.toSet());

        // 3. 过滤掉已领取的优惠券
        return allCoupons.stream()
                .filter(coupon -> !claimedCouponIds.contains(coupon.getId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimCoupon(Long userId, Long couponId) {
        // 1. 检查优惠券是否存在且有效
        Coupon coupon = getById(couponId);
        if (coupon == null || coupon.getStatus() != 1) {
            throw new BusinessException("优惠券不存在或已失效");
        }
        if (coupon.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("优惠券已过期");
        }
        if (coupon.getTotalCount() > 0 && coupon.getUsedCount() >= coupon.getTotalCount()) {
            throw new BusinessException("优惠券已被领完");
        }

        // 2. 检查用户是否已领取过
        Long count = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId));
        if (count > 0) {
            throw new BusinessException("您已领取过该优惠券");
        }

        // 3. 创建用户优惠券记录（领取后30天过期）
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(0);
        userCoupon.setGetTime(LocalDateTime.now());
        userCoupon.setExpireTime(LocalDateTime.now().plusDays(30));
        userCouponMapper.insert(userCoupon);

        // 4. 更新已领取数量
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        updateById(coupon);

        log.info("用户领取优惠券成功: userId={}, couponId={}", userId, couponId);
    }

    @Override
    public List<UserCoupon> getUserCoupons(Long userId) {
        return userCouponMapper.selectUserCoupons(userId);
    }

    @Override
    public List<UserCoupon> getAvailableUserCoupons(Long userId) {
        return userCouponMapper.selectAvailableCoupons(userId);
    }

    @Override
    public BigDecimal calculateDiscount(Long userCouponId, Long userId, BigDecimal orderAmount) {
        if (userCouponId == null) {
            return orderAmount;
        }

        // 查询用户优惠券
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null || !userCoupon.getUserId().equals(userId)) {
            throw new BusinessException("优惠券不存在");
        }
        if (userCoupon.getStatus() != 0) {
            throw new BusinessException("优惠券已使用或已过期");
        }
        // 检查用户优惠券是否过期（领取后30天）
        if (userCoupon.getExpireTime() != null && userCoupon.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("优惠券已过期");
        }

        // 查询优惠券详情
        Coupon coupon = getById(userCoupon.getCouponId());
        if (coupon == null || coupon.getStatus() != 1) {
            throw new BusinessException("优惠券已失效");
        }
        if (coupon.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("优惠券已过期");
        }

        // 检查最低消费
        if (orderAmount.compareTo(coupon.getMinAmount()) < 0) {
            throw new BusinessException("订单金额未达到最低消费要求：" + coupon.getMinAmount() + "元");
        }

        // 计算优惠金额
        BigDecimal discount;
        switch (coupon.getCouponType()) {
            case 1: // 满减券
                discount = coupon.getDiscountValue();
                break;
            case 2: // 折扣券
                discount = orderAmount.multiply(BigDecimal.ONE.subtract(coupon.getDiscountValue()))
                        .setScale(2, RoundingMode.HALF_UP);
                break;
            case 3: // 立减券
                discount = coupon.getDiscountValue();
                break;
            default:
                discount = BigDecimal.ZERO;
        }

        // 优惠金额不能超过订单金额
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }

        return orderAmount.subtract(discount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useCoupon(Long userCouponId, Long userId, Long orderId) {
        if (userCouponId == null) {
            return;
        }

        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null || !userCoupon.getUserId().equals(userId)) {
            throw new BusinessException("优惠券不存在");
        }
        if (userCoupon.getStatus() != 0) {
            throw new BusinessException("优惠券已使用或已过期");
        }

        userCoupon.setStatus(1);
        userCoupon.setOrderId(orderId);
        userCoupon.setUseTime(LocalDateTime.now());
        userCouponMapper.updateById(userCoupon);

        log.info("优惠券使用成功: userCouponId={}, orderId={}", userCouponId, orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantCouponToUser(Long userId, Long couponId) {
        Coupon coupon = getById(couponId);
        if (coupon == null || coupon.getStatus() != 1) {
            throw new BusinessException("优惠券不存在或已失效");
        }
        if (coupon.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("优惠券已过期");
        }
        if (coupon.getTotalCount() > 0 && coupon.getUsedCount() >= coupon.getTotalCount()) {
            throw new BusinessException("优惠券已被领完");
        }

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(0);
        userCoupon.setGetTime(LocalDateTime.now());
        userCoupon.setExpireTime(LocalDateTime.now().plusDays(30));
        userCouponMapper.insert(userCoupon);

        coupon.setUsedCount(coupon.getUsedCount() + 1);
        updateById(coupon);

        log.info("发放优惠券给用户: userId={}, couponId={}", userId, couponId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreCoupon(Long userCouponId, Long userId, Long orderId) {
        if (userCouponId == null) {
            return;
        }

        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null || !userCoupon.getUserId().equals(userId)) {
            log.warn("优惠券不存在或不属于该用户: userCouponId={}, userId={}", userCouponId, userId);
            return;
        }

        // 只有已使用状态的优惠券才能退还
        if (userCoupon.getStatus() != 1) {
            log.warn("优惠券状态不是已使用，无需退还: userCouponId={}, status={}", userCouponId, userCoupon.getStatus());
            return;
        }

        // 恢复为未使用状态
        userCoupon.setStatus(0);
        userCoupon.setOrderId(null);
        userCoupon.setUseTime(null);
        userCouponMapper.updateById(userCoupon);

        log.info("优惠券退还成功: userCouponId={}, orderId={}", userCouponId, orderId);
    }
}
