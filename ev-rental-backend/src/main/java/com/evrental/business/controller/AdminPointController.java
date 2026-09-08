package com.evrental.business.controller;

import com.evrental.business.service.PointService;
import com.evrental.common.result.R;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 积分管理控制器 - 管理员端
 */
@RestController
@RequestMapping("/api/admin/points")
@RequiredArgsConstructor
public class AdminPointController {

    private final PointService pointService;

    /**
     * 管理员手动调整用户积分
     */
    @PostMapping("/adjust")
    public R<Void> adjust(@RequestBody AdjustRequest req) {
        pointService.adminAdjustPoints(req.getUserId(), req.getPoints(), req.getRemark());
        return R.ok();
    }

    /**
     * 查看用户积分历史
     */
    @GetMapping("/history/{userId}")
    public R<?> userHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return R.ok(pointService.getPointHistory(userId, page, size));
    }

    @Data
    static class AdjustRequest {
        private Long userId;
        private int points;
        private String remark;
    }
}
