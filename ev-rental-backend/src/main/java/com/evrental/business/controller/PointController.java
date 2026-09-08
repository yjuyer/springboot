package com.evrental.business.controller;

import com.evrental.business.service.PointService;
import com.evrental.common.result.R;
import com.evrental.common.security.LoginUser;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 积分服务控制器
 */
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    /**
     * 我的积分历史
     */
    @GetMapping("/history")
    public R<Map<String, Object>> history(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @AuthenticationPrincipal LoginUser user) {
        return R.ok(pointService.getPointHistory(user.getUserId(), page, size));
    }

    /**
     * 我的当前积分
     */
    @GetMapping("/balance")
    public R<Integer> balance(@AuthenticationPrincipal LoginUser user) {
        return R.ok(pointService.getUserPoints(user.getUserId()));
    }

    /**
     * 积分兑换优惠券
     */
    @PostMapping("/exchange/coupon")
    public R<Void> exchangeCoupon(@RequestBody ExchangeRequest req,
                                   @AuthenticationPrincipal LoginUser user) {
        pointService.exchangeCoupon(user.getUserId(), req.getPointCost(), req.getCouponId());
        return R.ok();
    }

    /**
     * 积分兑换免费取消次数
     */
    @PostMapping("/exchange/free-cancel")
    public R<Void> exchangeFreeCancel(@RequestBody ExchangeRequest req,
                                       @AuthenticationPrincipal LoginUser user) {
        pointService.exchangeFreeCancel(user.getUserId(), req.getPointCost());
        return R.ok();
    }

    @Data
    static class ExchangeRequest {
        private int pointCost;
        private Long couponId;
    }
}
