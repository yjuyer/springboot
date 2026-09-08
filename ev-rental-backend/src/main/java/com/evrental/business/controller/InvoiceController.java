package com.evrental.business.controller;

import com.evrental.business.service.InvoiceService;
import com.evrental.common.result.R;
import com.evrental.common.security.LoginUser;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 发票管理控制器 - 用户端
 */
@RestController
@RequestMapping("/api/invoice")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    /**
     * 申请发票
     */
    @PostMapping("/apply")
    public R<Void> applyInvoice(@RequestBody ApplyRequest req,
                                @AuthenticationPrincipal LoginUser user) {
        invoiceService.applyInvoice(user.getUserId(), req.getOrderId(),
                req.getInvoiceType(), req.getInvoiceTitle(),
                req.getTaxNumber(), req.getEmail());
        return R.ok();
    }

    /**
     * 我的发票列表
     */
    @GetMapping("/my")
    public R<Map<String, Object>> myInvoices(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @AuthenticationPrincipal LoginUser user) {
        return R.ok(invoiceService.getUserInvoices(user.getUserId(), page, size, status));
    }

    /**
     * 发票详情
     */
    @GetMapping("/detail/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        return R.ok(Map.of(
                "invoice", invoiceService.getInvoiceDetail(id),
                "items", invoiceService.getInvoiceItems(id)
        ));
    }

    @Data
    static class ApplyRequest {
        private Long orderId;
        private Integer invoiceType;
        private String invoiceTitle;
        private String taxNumber;
        private String email;
    }
}
