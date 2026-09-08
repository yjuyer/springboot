package com.evrental.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evrental.business.entity.DepositRecord;
import com.evrental.business.entity.DepositRefund;
import com.evrental.business.mapper.DepositRefundMapper;
import com.evrental.business.service.DepositService;
import com.evrental.common.result.R;
import com.evrental.common.security.LoginUser;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员 - 押金管理
 */
@RestController
@RequestMapping("/api/admin/deposit")
@RequiredArgsConstructor
public class DepositController {

    private final DepositService depositService;
    private final DepositRefundMapper refundMapper;

    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        return R.ok(depositService.getDepositStats());
    }

    @GetMapping("/list")
    public R<IPage<DepositRecord>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderNo) {
        return R.ok(depositService.pageRecords(pageNum, pageSize, status, orderNo));
    }

    @GetMapping("/detail/{orderNo}")
    public R<DepositRecord> detail(@PathVariable String orderNo) {
        return R.ok(depositService.getByOrderNo(orderNo));
    }

    @PostMapping("/refund")
    public R<Void> refund(@RequestBody RefundReq req,
                          @AuthenticationPrincipal LoginUser user) {
        depositService.refundDeposit(req.getOrderId(), req.getRefundType(),
                req.getReason(), user.getUserId());
        return R.ok();
    }

    @GetMapping("/refunds")
    public R<List<DepositRefund>> refunds(
            @RequestParam(required = false) String orderNo) {
        LambdaQueryWrapper<DepositRefund> wrapper = new LambdaQueryWrapper<DepositRefund>()
                .orderByDesc(DepositRefund::getCreateTime);
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.eq(DepositRefund::getOrderNo, orderNo);
        }
        return R.ok(refundMapper.selectList(wrapper));
    }

    @Data
    static class RefundReq {
        private Long orderId;
        private Integer refundType;
        private String reason;
    }
}