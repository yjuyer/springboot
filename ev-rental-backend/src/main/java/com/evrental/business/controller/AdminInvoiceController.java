package com.evrental.business.controller;

import com.evrental.business.service.InvoiceService;
import com.evrental.common.result.R;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 发票管理控制器 - 管理员端
 */
@RestController
@RequestMapping("/api/admin/invoice")
@RequiredArgsConstructor
public class AdminInvoiceController {

    private final InvoiceService invoiceService;

    /**
     * 所有发票列表
     */
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        return R.ok(invoiceService.getAllInvoices(page, size, status));
    }

    /**
     * 审核开票
     */
    @PostMapping("/audit")
    public R<Void> audit(@RequestBody AuditRequest req) {
        invoiceService.auditInvoice(req.getInvoiceId(), req.getInvoiceNo(),
                req.getInvoiceFileUrl(), req.isApproved(), req.getRejectReason());
        return R.ok();
    }

    /**
     * 标记已发送
     */
    @PostMapping("/send/{id}")
    public R<Void> send(@PathVariable Long id) {
        invoiceService.markSent(id);
        return R.ok();
    }

    @Data
    static class AuditRequest {
        private Long invoiceId;
        private String invoiceNo;
        private String invoiceFileUrl;
        private boolean approved;
        private String rejectReason;
    }
}
