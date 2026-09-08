package com.evrental.business.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evrental.business.entity.RentalOrder;
import com.evrental.business.service.DepositService;
import com.evrental.business.service.RentalOrderService;
import com.evrental.common.result.R;
import com.evrental.common.security.LoginUser;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 管理员 - 订单管理
 */
@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final RentalOrderService orderService;
    private final DepositService depositService;

    @GetMapping("/list")
    public R<IPage<RentalOrder>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status) {
        return R.ok(orderService.pageOrders(null, status, orderNo, pageNum, pageSize));
    }

    @GetMapping("/detail/{id}")
    public R<RentalOrder> detail(@PathVariable Long id) {
        return R.ok(orderService.getById(id));
    }

    @PostMapping("/pickup")
    public R<Void> pickup(@RequestBody VehicleReq req) {
        orderService.pickupVehicle(req.getOrderId(), req.getBattery(), req.getMileage());
        return R.ok();
    }

    @PostMapping("/return/confirm/{orderId}")
    public R<Void> doReturn(@PathVariable Long orderId, @RequestBody ReturnReq req) {
        orderService.confirmReturn(orderId, req.getBattery(), req.getMileage());
        return R.ok();
    }

    @PostMapping("/refund/initiate/{orderId}")
    public R<Void> initiateRefund(@PathVariable Long orderId,
                                   @AuthenticationPrincipal LoginUser user) {
        depositService.initiateRefund(orderId, user.getUserId());
        return R.ok();
    }

    @PostMapping("/refund/complete/{orderId}")
    public R<Void> completeRefund(@PathVariable Long orderId,
                                   @AuthenticationPrincipal LoginUser user) {
        depositService.refundDeposit(orderId, null, null, user.getUserId());
        return R.ok();
    }

    @Data
    static class VehicleReq {
        private Long orderId;
        private BigDecimal battery;
        private BigDecimal mileage;
    }

    @Data
    static class ReturnReq {
        private BigDecimal battery;
        private BigDecimal mileage;
    }

    @Data
    static class RefundReq {
        private Long orderId;
        private Integer refundType;
        private String reason;
    }
}