package com.evrental.business.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evrental.business.dto.CreateOrderDTO;
import com.evrental.business.dto.PayDTO;
import com.evrental.business.entity.RentalOrder;
import com.evrental.business.entity.Vehicle;
import com.evrental.business.mapper.RentalOrderMapper;
import com.evrental.business.mapper.VehicleMapper;
import com.evrental.business.service.RentalOrderService;
import com.evrental.business.service.CouponService;
import com.evrental.business.service.CreditService;
import com.evrental.business.service.DepositService;
import com.evrental.business.service.NotificationService;
import com.evrental.business.service.PointService;
import com.evrental.common.enums.OrderStatusEnum;
import com.evrental.common.enums.VehicleStatusEnum;
import com.evrental.common.exception.BusinessException;
import com.evrental.common.lock.RedisLock;
import com.evrental.common.lock.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 订单服务实现类
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>创建订单 - 使用Redis分布式锁防止重复下单</li>
 *   <li>订单支付 - 更新订单状态为已支付</li>
 *   <li>取消订单 - 恢复车辆库存</li>
 *   <li>取车确认 - 管理员确认用户取车</li>
 *   <li>还车确认 - 管理员确认用户还车，更新车辆归属门店</li>
 * </ul>
 *
 * <p>订单状态流转：</p>
 * <pre>
 * 待支付(0) → 已支付(1) → 待取车(2) → 租赁中(3) → 待还车(4) → 已完成(5)
 *      │          │            │                        │
 *      │          │            └──→ 已取消(6)           │
 *      └──→ 已取消(6)                                   │
 *                                已完成(5) → 退款中(7) → 已退款(8)
 * </pre>
 *
 * @author ev-rental-team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RentalOrderServiceImpl extends ServiceImpl<RentalOrderMapper, RentalOrder>
        implements RentalOrderService {

    private final VehicleMapper vehicleMapper;
    private final RedisLock distributedLock;
    private final StockService stockService;
    private final DepositService depositService;
    private final CouponService couponService;
    private final NotificationService notificationService;
    private final com.evrental.system.service.MemberService memberService;
    private final CreditService creditService;
    private final com.evrental.system.mapper.SysUserMapper userMapper;
    private final PointService pointService;

    @Override
    public IPage<RentalOrder> pageOrders(Long userId, Integer status, String orderNo, int page, int size) {
        return baseMapper.selectOrderPage(new Page<>(page, size), userId, status, orderNo);
    }

    @Override
    public RentalOrder getOrderDetail(Long orderId) {
        return baseMapper.selectOrderDetail(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RentalOrder createOrder(Long userId, CreateOrderDTO dto) {
        String userLockKey = stockService.getOrderLockKey(userId);
        String userLockValue = distributedLock.tryLock(userLockKey, 15, TimeUnit.SECONDS);
        if (userLockValue == null) {
            throw new BusinessException("您有订单正在处理中，请勿重复提交");
        }

        try {
            String vehicleLockKey = stockService.getRentalLockKey(dto.getVehicleId());
            String vehicleLockValue = distributedLock.tryLock(vehicleLockKey, 10, TimeUnit.SECONDS);
            if (vehicleLockValue == null) {
                throw new BusinessException("该车辆正在被其他用户预订，请稍后重试");
            }

            try {
                if (!stockService.deductStock(dto.getVehicleId())) {
                    throw new BusinessException("该车辆已被租出，暂时不可预订");
                }

                try {
                    return doCreateOrder(userId, dto);
                } catch (Exception e) {
                    stockService.restoreStock(dto.getVehicleId());
                    throw e;
                }

            } finally {
                distributedLock.unlock(vehicleLockKey, vehicleLockValue);
            }
        } finally {
            distributedLock.unlock(userLockKey, userLockValue);
        }
    }

    private RentalOrder doCreateOrder(Long userId, CreateOrderDTO dto) {
        Vehicle vehicle = vehicleMapper.selectById(dto.getVehicleId());
        if (vehicle == null) {
            throw new BusinessException("车辆不存在");
        }
        com.evrental.system.entity.SysUser user = userMapper.selectById(userId);
        if (user != null && user.getCreditScore() != null && user.getCreditScore() < 80) {
            throw new BusinessException("信誉积分不足80分，暂时无法租车");
        }
        if (vehicle.getVehicleStatus() != VehicleStatusEnum.IDLE.getCode()) {
            throw new BusinessException("该车辆当前不可租用");
        }
        if (dto.getPickupTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("取车时间不能早于当前时间");
        }
        if (!dto.getReturnTime().isAfter(dto.getPickupTime())) {
            throw new BusinessException("还车时间必须晚于取车时间");
        }
        Long conflictCount = baseMapper.selectCount(new LambdaQueryWrapper<RentalOrder>()
                .eq(RentalOrder::getVehicleId, dto.getVehicleId())
                .eq(RentalOrder::getDeleted, 0)
                .in(RentalOrder::getOrderStatus, 0, 1, 2, 3, 4, 5, 7)
                .lt(RentalOrder::getPickupTime, dto.getReturnTime())
                .gt(RentalOrder::getReturnTime, dto.getPickupTime()));
        if (conflictCount != null && conflictCount > 0) {
            throw new BusinessException("该时间段车辆已被预约，请重新选择租期");
        }

        long days = ChronoUnit.DAYS.between(dto.getPickupTime(), dto.getReturnTime());
        if (days < 1) days = 1;
        BigDecimal total = vehicle.getDailyPrice().multiply(BigDecimal.valueOf(days));

        // 计算优惠券折扣
        BigDecimal finalAmount = total;
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (dto.getUserCouponId() != null) {
            finalAmount = couponService.calculateDiscount(dto.getUserCouponId(), userId, total);
            discountAmount = total.subtract(finalAmount);
        }

        RentalOrder order = new RentalOrder();
        order.setOrderNo("EV" + IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        order.setVehicleId(dto.getVehicleId());
        order.setPickupStoreId(dto.getPickupStoreId());
        order.setReturnStoreId(dto.getReturnStoreId());
        order.setPickupTime(dto.getPickupTime());
        order.setReturnTime(dto.getReturnTime());
        order.setDailyPrice(vehicle.getDailyPrice());
        order.setRentalDays((int) days);
        order.setTotalAmount(finalAmount);
        order.setDepositAmount(vehicle.getDeposit());
        order.setOrderStatus(OrderStatusEnum.PENDING_PAY.getCode());
        order.setRemark(dto.getRemark());
        order.setCouponId(dto.getUserCouponId());
        order.setDiscountAmount(discountAmount);
        save(order);

        // 使用优惠券
        if (dto.getUserCouponId() != null) {
            couponService.useCoupon(dto.getUserCouponId(), userId, order.getId());
        }

        depositService.createDepositRecord(order);

        vehicleMapper.update(null, new LambdaUpdateWrapper<Vehicle>()
                .eq(Vehicle::getId, dto.getVehicleId())
                .eq(Vehicle::getVehicleStatus, VehicleStatusEnum.IDLE.getCode())
                .set(Vehicle::getVehicleStatus, VehicleStatusEnum.RESERVED.getCode()));

        // 发送订单创建通知
        notificationService.sendToUser(userId, "订单创建成功",
                String.format("您的订单 %s 已创建，订单金额 ¥%s，请在60秒内完成支付。", order.getOrderNo(), finalAmount),
                1, order.getId());

        log.info("创建订单成功: orderNo={}, 优惠金额={}", order.getOrderNo(), discountAmount);
        return order;
    }

    @Override
    @Transactional
    public void payOrder(Long userId, PayDTO dto) {
        RentalOrder order = getById(dto.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != OrderStatusEnum.PENDING_PAY.getCode()) {
            throw new BusinessException("订单状态异常，无法支付");
        }

        order.setOrderStatus(OrderStatusEnum.PAID.getCode());
        order.setPaidAmount(order.getTotalAmount().add(order.getDepositAmount()));
        updateById(order);

        // 发送支付成功通知
        notificationService.sendToUser(userId, "订单支付成功",
                String.format("您的订单 %s 已支付成功，支付金额 ¥%s（含押金 ¥%s），请按时取车。", order.getOrderNo(), order.getPaidAmount(), order.getDepositAmount()),
                1, order.getId());

        log.info("订单支付成功: orderNo={}", order.getOrderNo());
    }

    /**
     * 支付+押金冻结后自动转为待取车
     */
    @Transactional
    public void readyForPickup(Long orderId) {
        RentalOrder order = getById(orderId);
        if (order != null && order.getOrderStatus() == OrderStatusEnum.PAID.getCode()) {
            order.setOrderStatus(OrderStatusEnum.PENDING_PICKUP.getCode());
            updateById(order);

            // 发送待取车通知
            notificationService.sendToUser(order.getUserId(), "请按时取车",
                    String.format("您的订单 %s 押金已冻结，请在预约时间前到门店取车。", order.getOrderNo()),
                    1, order.getId());

            log.info("订单转为待取车: orderNo={}", order.getOrderNo());
        }
    }

    @Override
    @Transactional
    public Map<String, Object> cancelOrder(Long userId, Long orderId, String reason) {
        RentalOrder order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }

        int status = order.getOrderStatus();
        if (status != OrderStatusEnum.PENDING_PAY.getCode()
                && status != OrderStatusEnum.PAID.getCode()
                && status != OrderStatusEnum.PENDING_PICKUP.getCode()) {
            throw new BusinessException("当前状态无法取消");
        }

        // 计算取消手续费
        BigDecimal cancelFee = BigDecimal.ZERO;
        BigDecimal refundAmount = order.getTotalAmount();
        String feeDesc = "免费取消";
        boolean usedFreeCancel = false;

        // 待支付订单免费取消
        if (status == OrderStatusEnum.PENDING_PAY.getCode()) {
            feeDesc = "待支付订单取消，免费";
        }
        // 已支付/待取车的订单计算手续费
        else if ((status == OrderStatusEnum.PAID.getCode()
                || status == OrderStatusEnum.PENDING_PICKUP.getCode())
                && order.getPickupTime() != null) {
            long hoursUntilPickup = ChronoUnit.HOURS.between(LocalDateTime.now(), order.getPickupTime());

            // 检查会员免费取消权益
            boolean canFreeCancel = memberService.canFreeCancel(userId);

            if (canFreeCancel && hoursUntilPickup < 48) {
                // 使用会员免费取消权益
                usedFreeCancel = true;
                feeDesc = "会员免费取消权益（不扣手续费）";
            } else if (hoursUntilPickup < 24) {
                // 24小时内取消：扣20%
                cancelFee = order.getTotalAmount().multiply(new BigDecimal("0.20"));
                feeDesc = "取车前24小时内取消，扣除20%手续费";
            } else if (hoursUntilPickup < 48) {
                // 24-48小时取消：扣10%
                cancelFee = order.getTotalAmount().multiply(new BigDecimal("0.10"));
                feeDesc = "取车前24-48小时取消，扣除10%手续费";
            } else {
                // 48小时以上：免费取消
                feeDesc = "取车前48小时以上取消，免费";
            }
            refundAmount = order.getTotalAmount().subtract(cancelFee);
        }

        // 更新订单状态
        order.setOrderStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setCancelReason(reason);
        order.setDiscountAmount(cancelFee);
        updateById(order);

        // 使用免费取消次数
        if (usedFreeCancel) {
            memberService.useFreeCancel(userId);
        }

        if (status == OrderStatusEnum.PAID.getCode() || status == OrderStatusEnum.PENDING_PICKUP.getCode()) {
            creditService.changeCredit(userId, -2, "取消已支付订单", order.getId());
        }

        // 完成订单后累加消费金额（用于升级会员等级）
        if (status == OrderStatusEnum.PAID.getCode()) {
            memberService.addSpending(userId, order.getTotalAmount());
        }

        // 恢复车辆库存
        stockService.restoreStock(order.getVehicleId());
        vehicleMapper.update(null, new LambdaUpdateWrapper<Vehicle>()
                .eq(Vehicle::getId, order.getVehicleId())
                .set(Vehicle::getVehicleStatus, VehicleStatusEnum.IDLE.getCode()));

        // 发送订单取消通知
        notificationService.sendToUser(userId, "订单已取消",
                String.format("您的订单 %s 已取消。%s 退款金额 ¥%s。", order.getOrderNo(), feeDesc, refundAmount),
                1, order.getId());

        log.info("订单取消: orderNo={}, 手续费={}, 退款金额={}, 使用免费取消={}", order.getOrderNo(), cancelFee, refundAmount, usedFreeCancel);

        // 返回取消结果
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", order.getOrderNo());
        result.put("cancelFee", cancelFee);
        result.put("refundAmount", refundAmount);
        result.put("feeDesc", feeDesc);
        return result;
    }

    @Override
    @Transactional
    public void pickupVehicle(Long orderId, BigDecimal battery, BigDecimal mileage) {
        RentalOrder order = getById(orderId);
        int status = order.getOrderStatus();
        if (status != OrderStatusEnum.PAID.getCode()
                && status != OrderStatusEnum.PENDING_PICKUP.getCode()) {
            throw new BusinessException("订单状态异常，无法取车");
        }

        order.setOrderStatus(OrderStatusEnum.RENTING.getCode());
        order.setPickupTime(LocalDateTime.now());
        order.setPickupBattery(battery != null ? battery.intValue() : null);
        order.setPickupMileage(mileage);
        updateById(order);

        vehicleMapper.update(null, new LambdaUpdateWrapper<Vehicle>()
                .eq(Vehicle::getId, order.getVehicleId())
                .set(Vehicle::getVehicleStatus, VehicleStatusEnum.RENTING.getCode())
                .set(Vehicle::getCurrentBattery, battery != null ? battery.intValue() : null));
    }

    @Override
    @Transactional
    public void userPickup(Long userId, Long orderId) {
        RentalOrder order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != OrderStatusEnum.PENDING_PICKUP.getCode()) {
            throw new BusinessException("订单状态异常，无法取车");
        }

        // 检查是否在预约取车时间前1小时内
        if (order.getPickupTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime pickupTime = order.getPickupTime();
            LocalDateTime oneHourBefore = pickupTime.minusHours(1);

            if (now.isBefore(oneHourBefore)) {
                long minutesUntilPickup = ChronoUnit.MINUTES.between(now, pickupTime);
                long hoursUntilPickup = minutesUntilPickup / 60;
                long minutesRemaining = minutesUntilPickup % 60;
                throw new BusinessException(
                    String.format("距离预约取车时间还有%d小时%d分钟，请在预约时间前1小时内取车",
                            hoursUntilPickup, minutesRemaining));
            }
        }

        Vehicle vehicle = vehicleMapper.selectById(order.getVehicleId());

        order.setOrderStatus(OrderStatusEnum.RENTING.getCode());
        order.setPickupTime(LocalDateTime.now());
        order.setPickupBattery(vehicle != null ? vehicle.getCurrentBattery() : null);
        updateById(order);

        vehicleMapper.update(null, new LambdaUpdateWrapper<Vehicle>()
                .eq(Vehicle::getId, order.getVehicleId())
                .set(Vehicle::getVehicleStatus, VehicleStatusEnum.RENTING.getCode()));

        // 发送取车成功通知
        notificationService.sendToUser(userId, "取车成功",
                String.format("您的订单 %s 已确认取车，祝您旅途愉快！", order.getOrderNo()),
                1, order.getId());

        log.info("用户取车成功: orderNo={}", order.getOrderNo());
    }

    @Override
    @Transactional
    public void requestReturn(Long userId, Long orderId) {
        RentalOrder order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != OrderStatusEnum.RENTING.getCode()) {
            throw new BusinessException("订单状态异常，无法申请还车");
        }

        order.setOrderStatus(OrderStatusEnum.PENDING_RETURN.getCode());
        updateById(order);

        // 发送还车申请通知
        notificationService.sendToUser(userId, "还车申请已提交",
                String.format("您的订单 %s 还车申请已提交，请等待管理员确认。", order.getOrderNo()),
                1, order.getId());

        log.info("用户申请还车: orderNo={}", order.getOrderNo());
    }

    @Override
    @Transactional
    public void confirmReturn(Long orderId, BigDecimal battery, BigDecimal mileage) {
        RentalOrder order = getById(orderId);
        if (order == null || order.getOrderStatus() != OrderStatusEnum.PENDING_RETURN.getCode()) {
            throw new BusinessException("订单状态异常，无法确认还车");
        }

        LocalDateTime actualReturnTime = LocalDateTime.now();
        order.setOrderStatus(OrderStatusEnum.COMPLETED.getCode());
        order.setActualReturnTime(actualReturnTime);
        order.setReturnBattery(battery != null ? battery.intValue() : null);
        order.setReturnMileage(mileage);
        updateById(order);

        stockService.restoreStock(order.getVehicleId());
        vehicleMapper.update(null, new LambdaUpdateWrapper<Vehicle>()
                .eq(Vehicle::getId, order.getVehicleId())
                .set(Vehicle::getVehicleStatus, VehicleStatusEnum.IDLE.getCode())
                .set(Vehicle::getCurrentBattery, battery != null ? battery.intValue() : null)
                .set(Vehicle::getMileage, mileage)
                .set(Vehicle::getStoreId, order.getReturnStoreId()));  // 还车后车辆归属到还车门店

        if (order.getReturnTime() != null && actualReturnTime.isAfter(order.getReturnTime())) {
            creditService.changeCredit(order.getUserId(), -10, "超时还车", order.getId());
        } else {
            creditService.changeCredit(order.getUserId(), 5, "正常完成订单", order.getId());
        }

        // 完成订单后累加会员积分（订单金额1元=1积分，押金不算）
        memberService.addMemberPoints(order.getUserId(), order.getTotalAmount());
        // 同时记录积分明细
        pointService.addOrderPoints(order.getUserId(), order.getTotalAmount(), order.getId());

        // 累加消费金额并更新会员等级
        memberService.addSpending(order.getUserId(), order.getTotalAmount());

        // 发送还车成功通知
        notificationService.sendToUser(order.getUserId(), "还车成功，订单完成",
                String.format("您的订单 %s 已完成还车，消费金额 ¥%s，押金将在15天后自动退还。感谢使用e租出行！", order.getOrderNo(), order.getTotalAmount()),
                1, order.getId());

        log.info("还车确认成功: orderNo={}, 车辆已归属到还车门店: storeId={}", order.getOrderNo(), order.getReturnStoreId());
    }
}