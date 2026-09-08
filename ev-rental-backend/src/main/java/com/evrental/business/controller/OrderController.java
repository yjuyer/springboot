package com.evrental.business.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evrental.business.dto.CreateOrderDTO;
import com.evrental.business.dto.PayDTO;
import com.evrental.business.entity.RentalOrder;
import com.evrental.business.service.RentalOrderService;
import com.evrental.common.result.R;
import com.evrental.common.security.LoginUser;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户订单控制器
 */
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final RentalOrderService orderService;

    /** 从SecurityContext获取当前登录用户 */
    private LoginUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) {
            return (LoginUser) auth.getPrincipal();
        }
        return null;
    }

    @PostMapping("/create")
    public R<RentalOrder> create(@RequestBody CreateOrderDTO dto) {
        LoginUser user = getCurrentUser();
        if (user == null) return R.error("请先登录");
        return R.ok(orderService.createOrder(user.getUserId(), dto));
    }

    @PostMapping("/pay")
    public R<Void> pay(@RequestBody PayDTO dto) {
        LoginUser user = getCurrentUser();
        if (user == null) return R.error("请先登录");
        orderService.payOrder(user.getUserId(), dto);
        return R.ok();
    }

    @PostMapping("/cancel/{id}")
    public R<Map<String, Object>> cancel(@PathVariable Long id,
                                         @RequestBody(required = false) CancelReq req) {
        LoginUser user = getCurrentUser();
        if (user == null) return R.error("请先登录");
        Map<String, Object> result = orderService.cancelOrder(user.getUserId(), id,
                req != null ? req.getReason() : "用户主动取消");
        return R.ok(result);
    }

    @PostMapping("/return-request/{id}")
    public R<Void> requestReturn(@PathVariable Long id) {
        LoginUser user = getCurrentUser();
        if (user == null) return R.error("请先登录");
        orderService.requestReturn(user.getUserId(), id);
        return R.ok();
    }

    @PostMapping("/pickup/{id}")
    public R<Void> pickup(@PathVariable Long id) {
        LoginUser user = getCurrentUser();
        if (user == null) return R.error("请先登录");
        orderService.userPickup(user.getUserId(), id);
        return R.ok();
    }

    @GetMapping("/my")
    public R<IPage<RentalOrder>> myOrders(@RequestParam(required = false) Integer status,
                                          @RequestParam(defaultValue = "1") int pageNum,
                                          @RequestParam(defaultValue = "10") int pageSize) {
        LoginUser user = getCurrentUser();
        if (user == null) return R.error(401, "请先登录");
        return R.ok(orderService.pageOrders(user.getUserId(), status, null, pageNum, pageSize));
    }

    @GetMapping("/detail/{id}")
    public R<RentalOrder> detail(@PathVariable Long id) {
        LoginUser user = getCurrentUser();
        if (user == null) return R.error(401, "请先登录");
        RentalOrder order = orderService.getOrderDetail(id);
        if (order == null || !order.getUserId().equals(user.getUserId())) {
            return R.error(403, "无权访问该订单");
        }
        return R.ok(order);
    }

    @Data
    static class CancelReq { private String reason; }
}
