package com.evrental.business.controller;

import com.evrental.business.entity.Coupon;
import com.evrental.business.entity.UserCoupon;
import com.evrental.business.service.CouponService;
import com.evrental.common.result.R;
import com.evrental.common.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券控制器
 *
 * <p>提供以下接口：</p>
 * <ul>
 *   <li>GET  /api/coupon/list       - 获取可领取优惠券列表（公开接口，登录用户过滤已领取）</li>
 *   <li>POST /api/coupon/claim/{id} - 领取优惠券（需登录）</li>
 *   <li>GET  /api/coupon/my         - 获取我的优惠券列表（需登录）</li>
 *   <li>GET  /api/coupon/available  - 获取下单时可用的优惠券（需登录）</li>
 *   <li>GET  /api/coupon/calculate  - 计算优惠券折扣后金额（需登录）</li>
 * </ul>
 *
 * @author ev-rental-team
 */
@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    /**
     * 获取当前登录用户（可能为null）
     */
    private LoginUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) {
            return (LoginUser) auth.getPrincipal();
        }
        return null;
    }

    /**
     * 获取可领取的优惠券列表（排除已领取的）
     */
    @GetMapping("/list")
    public R<List<Coupon>> getAvailableCoupons() {
        LoginUser loginUser = getCurrentUser();
        Long userId = loginUser != null ? loginUser.getUserId() : null;
        return R.ok(couponService.getAvailableCoupons(userId));
    }

    /**
     * 领取优惠券
     */
    @PostMapping("/claim/{id}")
    public R<Void> claimCoupon(@PathVariable Long id) {
        LoginUser loginUser = getCurrentUser();
        if (loginUser == null) {
            return R.error("请先登录");
        }
        couponService.claimCoupon(loginUser.getUserId(), id);
        return R.ok();
    }

    /**
     * 我的优惠券列表
     */
    @GetMapping("/my")
    public R<List<UserCoupon>> getMyCoupons() {
        LoginUser loginUser = getCurrentUser();
        if (loginUser == null) {
            return R.error("请先登录");
        }
        return R.ok(couponService.getUserCoupons(loginUser.getUserId()));
    }

    /**
     * 下单时可用的优惠券列表
     */
    @GetMapping("/available")
    public R<List<UserCoupon>> getAvailableCouponsForOrder() {
        LoginUser loginUser = getCurrentUser();
        if (loginUser == null) {
            return R.error("请先登录");
        }
        return R.ok(couponService.getAvailableUserCoupons(loginUser.getUserId()));
    }

    /**
     * 计算优惠券折扣后的金额
     */
    @GetMapping("/calculate")
    public R<BigDecimal> calculateDiscount(@RequestParam Long userCouponId,
                                           @RequestParam BigDecimal orderAmount) {
        LoginUser loginUser = getCurrentUser();
        if (loginUser == null) {
            return R.error("请先登录");
        }
        BigDecimal finalAmount = couponService.calculateDiscount(
                userCouponId, loginUser.getUserId(), orderAmount);
        return R.ok(finalAmount);
    }
}
