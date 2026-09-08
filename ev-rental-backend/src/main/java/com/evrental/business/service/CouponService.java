package com.evrental.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evrental.business.entity.Coupon;
import com.evrental.business.entity.UserCoupon;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券服务接口
 */
public interface CouponService extends IService<Coupon> {

    /**
     * 获取可领取的优惠券列表（排除用户已领取的）
     */
    List<Coupon> getAvailableCoupons(Long userId);

    /**
     * 用户领取优惠券
     */
    void claimCoupon(Long userId, Long couponId);

    /**
     * 获取用户的所有优惠券
     */
    List<UserCoupon> getUserCoupons(Long userId);

    /**
     * 获取用户可用的优惠券（下单时选择）
     */
    List<UserCoupon> getAvailableUserCoupons(Long userId);

    /**
     * 计算优惠金额
     * @param userCouponId 用户优惠券ID
     * @param orderAmount 订单金额
     * @return 优惠后金额
     */
    BigDecimal calculateDiscount(Long userCouponId, Long userId, BigDecimal orderAmount);

    /**
     * 使用优惠券（标记为已使用）
     */
    void useCoupon(Long userCouponId, Long userId, Long orderId);

    /**
     * 退还优惠券（订单取消时恢复为未使用状态）
     */
    void restoreCoupon(Long userCouponId, Long userId, Long orderId);

    /**
     * 直接发放优惠券给用户（管理员/积分兑换使用，跳过用户限领校验）
     */
    void grantCouponToUser(Long userId, Long couponId);
}
