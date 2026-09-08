package com.evrental.business.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evrental.business.entity.VehicleReview;
import com.evrental.business.mapper.VehicleReviewMapper;
import com.evrental.common.result.R;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/** 管理员 - 评价管理 */
@RestController
@RequestMapping("/api/admin/review")
@RequiredArgsConstructor
public class AdminReviewController {

    private final VehicleReviewMapper reviewMapper;

    @GetMapping("/list")
    public R<IPage<VehicleReview>> list(@RequestParam(defaultValue = "1") int pageNum,
                                        @RequestParam(defaultValue = "10") int pageSize,
                                        @RequestParam(required = false) Long vehicleId,
                                        @RequestParam(required = false) Integer status) {
        return R.ok(reviewMapper.selectReviewPage(new Page<>(pageNum, pageSize), vehicleId, status));
    }

    @PutMapping("/audit/{id}")
    public R<Void> audit(@PathVariable Long id, @RequestBody AuditReq req) {
        VehicleReview review = new VehicleReview();
        review.setId(id);
        review.setStatus(req.getStatus());
        review.setReply(req.getReply());
        if (req.getReply() != null && !req.getReply().isBlank()) {
            review.setReplyTime(LocalDateTime.now());
        }
        reviewMapper.updateById(review);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        reviewMapper.deleteById(id);
        return R.ok();
    }

    @Data
    static class AuditReq {
        private Integer status;
        private String reply;
    }
}
