package com.evrental.business.controller;

import com.evrental.business.service.AlipayService;
import com.evrental.common.result.R;
import com.evrental.common.security.LoginUser;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付控制器
 *
 * 接口列表：
 *   POST /api/pay/create       → 创建支付，返回二维码 + 订单详情
 *   GET  /api/pay/qrcode/{orderNo} → 生成二维码图片，返回图片地址
 *   GET  /api/pay/order/{orderNo}  → 获取支付订单详情
 *   GET  /api/pay/status       → 查询支付状态（轮询）
 *   POST /api/pay/confirm      → 确认支付（演示模式）
 *   POST /api/pay/notify       → 支付宝异步回调
 */
@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {

    private final AlipayService alipayService;

    /**
     * 创建支付
     * 返回完整支付信息 + 二维码图片地址
     */
    @PostMapping("/create")
    public R<Map<String, Object>> createPay(@RequestBody PayRequest req,
                                            @AuthenticationPrincipal LoginUser user) {
        return R.ok(alipayService.createPayUrl(req.getOrderId()));
    }

    /**
     * 生成支付二维码图片
     * GET /api/pay/qrcode/{orderNo}
     * 返回二维码PNG图片的访问地址
     */
    @GetMapping("/qrcode/{orderNo}")
    public R<Map<String, String>> generateQRCode(@PathVariable String orderNo) {
        String qrCodeUrl = alipayService.generateQRCodeByOrderNo(orderNo);
        Map<String, String> data = new java.util.HashMap<>();
        data.put("qrCodeUrl", qrCodeUrl);
        return R.ok(data);
    }

    /**
     * 获取支付订单详情
     * GET /api/pay/order/{orderNo}
     */
    @GetMapping("/order/{orderNo}")
    public R<Map<String, Object>> orderDetail(@PathVariable String orderNo) {
        return R.ok(alipayService.getPayDetail(orderNo));
    }

    /**
     * 查询支付状态（前端轮询此接口）
     */
    @GetMapping("/status")
    public R<Map<String, Object>> queryStatus(@RequestParam String orderNo) {
        return R.ok(alipayService.queryPayStatus(orderNo));
    }

    /**
     * 确认支付（演示模式）
     * 用户点击"我已支付"时调用
     */
    @PostMapping("/confirm")
    public R<Void> confirmPay(@RequestBody ConfirmRequest req,
                              @AuthenticationPrincipal LoginUser user) {
        alipayService.confirmPay(req.getOrderId(), req.getPayType());
        return R.ok();
    }

    /**
     * 支付宝异步回调接口
     * 支付宝在用户支付成功后会 POST 调用此接口
     * 必须返回 "success" 或 "fail"
     */
    @PostMapping("/notify")
    public String alipayNotify(@RequestParam Map<String, String> params) {
        return alipayService.handleNotify(params);
    }

    @Data
    static class PayRequest {
        private Long orderId;
    }

    @Data
    static class ConfirmRequest {
        private Long orderId;
        private Integer payType;
    }
}
