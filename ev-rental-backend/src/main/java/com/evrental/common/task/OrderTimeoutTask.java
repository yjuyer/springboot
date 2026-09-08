package com.evrental.common.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.evrental.business.entity.RentalOrder;
import com.evrental.business.entity.Vehicle;
import com.evrental.business.mapper.RentalOrderMapper;
import com.evrental.business.mapper.VehicleMapper;
import com.evrental.business.service.CouponService;
import com.evrental.business.service.NotificationService;
import com.evrental.common.enums.OrderStatusEnum;
import com.evrental.common.enums.VehicleStatusEnum;
import com.evrental.common.lock.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务 - 订单超时自动取消
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>每秒扫描一次待支付订单</li>
 *   <li>超过60秒未支付的订单自动取消</li>
 *   <li>恢复车辆库存和状态</li>
 *   <li>退还已使用的优惠券</li>
 * </ul>
 *
 * @author ev-rental
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final RentalOrderMapper orderMapper;
    private final VehicleMapper vehicleMapper;
    private final StockService stockService;
    private final CouponService couponService;
    private final NotificationService notificationService;

    /** 订单支付超时时间（秒） */
    private static final int ORDER_TIMEOUT_SECONDS = 60;

    /**
     * 每秒执行一次，检查超时未支付的订单并自动取消
     */
    @Scheduled(fixedRate = 1000)
    @Transactional(rollbackFor = Exception.class)
    public void cancelTimeoutOrders() {
        // 计算超时时间点
        LocalDateTime timeoutTime = LocalDateTime.now().minusSeconds(ORDER_TIMEOUT_SECONDS);

        // 查询所有超时的待支付订单
        List<RentalOrder> timeoutOrders = orderMapper.selectList(
                new LambdaQueryWrapper<RentalOrder>()
                        .eq(RentalOrder::getOrderStatus, OrderStatusEnum.PENDING_PAY.getCode())
                        .le(RentalOrder::getCreateTime, timeoutTime)
                        .eq(RentalOrder::getDeleted, 0));

        if (timeoutOrders.isEmpty()) {
            return;
        }

        int cancelledCount = 0;
        for (RentalOrder order : timeoutOrders) {
            try {
                cancelOrder(order);
                cancelledCount++;
                log.info("订单超时自动取消成功: orderNo={}, 创建时间={}, 超时秒数={}",
                        order.getOrderNo(), order.getCreateTime(), ORDER_TIMEOUT_SECONDS);
            } catch (Exception e) {
                log.error("订单超时自动取消失败: orderNo={}, error={}", order.getOrderNo(), e.getMessage());
            }
        }

        if (cancelledCount > 0) {
            log.info("===== 订单超时自动取消任务完成，本次共取消 {} 笔订单 =====", cancelledCount);
        }
    }

    /**
     * 取消单个订单
     */
    private void cancelOrder(RentalOrder order) {
        // 1. 更新订单状态为已取消
        order.setOrderStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setCancelReason("超时未支付，系统自动取消");
        orderMapper.updateById(order);

        // 2. 恢复车辆库存
        stockService.restoreStock(order.getVehicleId());

        // 3. 恢复车辆状态为空闲
        vehicleMapper.update(null, new LambdaUpdateWrapper<Vehicle>()
                .eq(Vehicle::getId, order.getVehicleId())
                .set(Vehicle::getVehicleStatus, VehicleStatusEnum.IDLE.getCode()));

        // 4. 退还已使用的优惠券
        if (order.getCouponId() != null) {
            try {
                couponService.restoreCoupon(order.getCouponId(), order.getUserId(), order.getId());
                log.info("优惠券退还成功: couponId={}, orderId={}", order.getCouponId(), order.getId());
            } catch (Exception e) {
                log.error("优惠券退还失败: couponId={}, orderId={}, error={}",
                        order.getCouponId(), order.getId(), e.getMessage());
            }
        }

        // 5. 发送超时取消通知
        try {
            notificationService.sendToUser(order.getUserId(), "订单超时已取消",
                    String.format("您的订单 %s 因超过60秒未支付已被系统自动取消，已使用的优惠券已返还。", order.getOrderNo()),
                    1, order.getId());
        } catch (Exception e) {
            log.warn("发送超时取消通知失败: orderNo={}", order.getOrderNo());
        }
    }
}
