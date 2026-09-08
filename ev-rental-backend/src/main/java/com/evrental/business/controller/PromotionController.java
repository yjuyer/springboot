package com.evrental.business.controller;

import com.evrental.business.entity.PromotionActivity;
import com.evrental.business.service.PromotionService;
import com.evrental.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 促销活动管理控制器
 */
@RestController
@RequestMapping("/api/admin/promotion")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    /**
     * 创建活动
     */
    @PostMapping("/create")
    public R<Void> create(@RequestBody PromotionActivity activity) {
        promotionService.createActivity(activity);
        return R.ok();
    }

    /**
     * 更新活动
     */
    @PutMapping("/update")
    public R<Void> update(@RequestBody PromotionActivity activity) {
        promotionService.updateActivity(activity);
        return R.ok();
    }

    /**
     * 删除活动
     */
    @DeleteMapping("/delete/{id}")
    public R<Void> delete(@PathVariable Long id) {
        promotionService.deleteActivity(id);
        return R.ok();
    }

    /**
     * 启用/禁用活动
     */
    @PostMapping("/toggle/{id}")
    public R<Void> toggle(@PathVariable Long id) {
        promotionService.toggleActivity(id);
        return R.ok();
    }

    /**
     * 活动列表
     */
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        return R.ok(promotionService.listActivities(page, size, status));
    }

    /**
     * 获取当前有效活动（用户端下单时调用）
     */
    @GetMapping("/active")
    public R<Object> active() {
        return R.ok(promotionService.getActiveActivities());
    }
}
