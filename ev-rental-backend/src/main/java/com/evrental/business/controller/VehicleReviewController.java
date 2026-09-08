package com.evrental.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evrental.business.entity.RentalOrder;
import com.evrental.business.entity.VehicleReview;
import com.evrental.business.mapper.RentalOrderMapper;
import com.evrental.business.mapper.VehicleReviewMapper;
import com.evrental.business.service.CreditService;
import com.evrental.common.enums.OrderStatusEnum;
import com.evrental.common.exception.BusinessException;
import com.evrental.common.result.R;
import com.evrental.common.security.LoginUser;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/** 用户端车辆评价 */
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class VehicleReviewController {

    private final VehicleReviewMapper reviewMapper;
    private final RentalOrderMapper orderMapper;
    private final CreditService creditService;

    @GetMapping("/vehicle/{vehicleId}")
    public R<IPage<VehicleReview>> vehicleReviews(@PathVariable Long vehicleId,
                                                  @RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "5") int pageSize) {
        return R.ok(reviewMapper.selectReviewPage(new Page<>(pageNum, pageSize), vehicleId, 1));
    }

    @GetMapping("/vehicle/{vehicleId}/summary")
    public R<Map<String, Object>> summary(@PathVariable Long vehicleId) {
        Long count = reviewMapper.selectCount(new LambdaQueryWrapper<VehicleReview>()
                .eq(VehicleReview::getVehicleId, vehicleId)
                .eq(VehicleReview::getStatus, 1)
                .eq(VehicleReview::getDeleted, 0));
        Double avg = 0.0;
        if (count != null && count > 0) {
            java.util.List<VehicleReview> list = reviewMapper.selectList(new LambdaQueryWrapper<VehicleReview>()
                    .eq(VehicleReview::getVehicleId, vehicleId)
                    .eq(VehicleReview::getStatus, 1)
                    .eq(VehicleReview::getDeleted, 0));
            avg = list.stream().map(VehicleReview::getRating).filter(java.util.Objects::nonNull)
                    .mapToInt(Integer::intValue).average().orElse(0.0);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("count", count == null ? 0 : count);
        data.put("avgRating", Math.round(avg * 10.0) / 10.0);
        return R.ok(data);
    }

    @GetMapping("/order/{orderId}/status")
    public R<Map<String, Object>> orderReviewStatus(@AuthenticationPrincipal LoginUser user,
                                                    @PathVariable Long orderId) {
        RentalOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(user.getUserId())) {
            throw new BusinessException(403, "无权访问该订单");
        }
        Long count = reviewMapper.selectCount(new LambdaQueryWrapper<VehicleReview>()
                .eq(VehicleReview::getOrderId, orderId)
                .eq(VehicleReview::getDeleted, 0));
        Map<String, Object> data = new HashMap<>();
        data.put("reviewed", count != null && count > 0);
        data.put("canReview", order.getOrderStatus() != null
                && order.getOrderStatus() >= OrderStatusEnum.COMPLETED.getCode()
                && order.getOrderStatus() != OrderStatusEnum.CANCELLED.getCode());
        return R.ok(data);
    }

    @PostMapping("/submit")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> submit(@AuthenticationPrincipal LoginUser user, @RequestBody ReviewReq req) {
        if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5) {
            throw new BusinessException("评分必须在1-5分之间");
        }
        RentalOrder order = orderMapper.selectById(req.getOrderId());
        if (order == null || !order.getUserId().equals(user.getUserId())) {
            throw new BusinessException(403, "无权评价该订单");
        }
        if (order.getOrderStatus() == null || order.getOrderStatus() < OrderStatusEnum.COMPLETED.getCode()
                || order.getOrderStatus().equals(OrderStatusEnum.CANCELLED.getCode())) {
            throw new BusinessException("订单完成后才可评价");
        }
        Long exists = reviewMapper.selectCount(new LambdaQueryWrapper<VehicleReview>()
                .eq(VehicleReview::getOrderId, order.getId())
                .eq(VehicleReview::getDeleted, 0));
        if (exists != null && exists > 0) {
            throw new BusinessException("该订单已评价");
        }

        VehicleReview review = new VehicleReview();
        review.setOrderId(order.getId());
        review.setUserId(user.getUserId());
        review.setVehicleId(order.getVehicleId());
        review.setStoreId(order.getReturnStoreId());
        review.setRating(req.getRating());
        review.setTags(req.getTags());
        review.setContent(req.getContent());
        review.setAnonymous(req.getAnonymous() == null ? 0 : req.getAnonymous());
        review.setStatus(0);
        reviewMapper.insert(review);

        if (req.getRating() >= 5) {
            creditService.changeCredit(user.getUserId(), 2, "提交五星好评", order.getId());
        }
        return R.ok();
    }

    @Data
    static class ReviewReq {
        private Long orderId;
        private Integer rating;
        private String tags;
        private String content;
        private Integer anonymous;
    }
}
