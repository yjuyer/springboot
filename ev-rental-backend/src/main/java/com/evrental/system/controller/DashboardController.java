package com.evrental.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evrental.business.entity.RentalOrder;
import com.evrental.business.entity.Vehicle;
import com.evrental.business.mapper.RentalOrderMapper;
import com.evrental.business.mapper.VehicleMapper;
import com.evrental.common.result.R;
import com.evrental.system.entity.SysUser;
import com.evrental.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 管理员 - 数据统计仪表盘
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SysUserMapper userMapper;
    private final VehicleMapper vehicleMapper;
    private final RentalOrderMapper orderMapper;
    private final com.evrental.business.mapper.StoreMapper storeMapper;

    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();

        data.put("totalUsers", userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeleted, 0)));
        data.put("totalVehicles", vehicleMapper.selectCount(
                new LambdaQueryWrapper<Vehicle>().eq(Vehicle::getDeleted, 0)));
        data.put("activeOrders", orderMapper.selectCount(
                new LambdaQueryWrapper<RentalOrder>()
                        .in(RentalOrder::getOrderStatus, 1, 2, 3, 4)
                        .eq(RentalOrder::getDeleted, 0)));

        List<RentalOrder> completed = orderMapper.selectList(
                new LambdaQueryWrapper<RentalOrder>()
                        .eq(RentalOrder::getOrderStatus, 5)
                        .isNotNull(RentalOrder::getPaidAmount)
                        .eq(RentalOrder::getDeleted, 0));
        BigDecimal revenue = completed.stream()
                .map(RentalOrder::getPaidAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("totalRevenue", revenue);

        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime monthStart = todayStart.withDayOfMonth(1);
        data.put("todayOrders", orderMapper.selectCount(new LambdaQueryWrapper<RentalOrder>()
                .ge(RentalOrder::getCreateTime, todayStart).eq(RentalOrder::getDeleted, 0)));
        data.put("todayRevenue", completed.stream()
                .filter(o -> o.getCreateTime() != null && !o.getCreateTime().isBefore(todayStart))
                .map(RentalOrder::getPaidAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
        data.put("monthRevenue", completed.stream()
                .filter(o -> o.getCreateTime() != null && !o.getCreateTime().isBefore(monthStart))
                .map(RentalOrder::getPaidAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
        data.put("lowBatteryVehicles", vehicleMapper.selectCount(new LambdaQueryWrapper<Vehicle>()
                .lt(Vehicle::getCurrentBattery, 30).eq(Vehicle::getDeleted, 0)));
        data.put("pendingRefundOrders", orderMapper.selectCount(new LambdaQueryWrapper<RentalOrder>()
                .eq(RentalOrder::getOrderStatus, 5).eq(RentalOrder::getDeleted, 0)));

        return R.ok(data);
    }

    @GetMapping("/vehicleStatus")
    public R<List<Map<String, Object>>> vehicleStatus() {
        String[] labels = {"空闲", "已预约", "租赁中", "维修中", "充电中", "调度中"};
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            long count = vehicleMapper.selectCount(new LambdaQueryWrapper<Vehicle>()
                    .eq(Vehicle::getVehicleStatus, i).eq(Vehicle::getDeleted, 0));
            Map<String, Object> item = new HashMap<>();
            item.put("name", labels[i]);
            item.put("value", count);
            result.add(item);
        }
        return R.ok(result);
    }

    @GetMapping("/orderStatus")
    public R<List<Map<String, Object>>> orderStatus() {
        String[] labels = {"待支付", "已支付", "待取车", "租赁中", "待还车", "已完成", "已取消", "退款中", "已退款"};
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            long count = orderMapper.selectCount(new LambdaQueryWrapper<RentalOrder>()
                    .eq(RentalOrder::getOrderStatus, i).eq(RentalOrder::getDeleted, 0));
            Map<String, Object> item = new HashMap<>();
            item.put("name", labels[i]);
            item.put("value", count);
            result.add(item);
        }
        return R.ok(result);
    }

    @GetMapping("/popularVehicles")
    public R<List<Map<String, Object>>> popularVehicles() {
        List<RentalOrder> orders = orderMapper.selectList(new LambdaQueryWrapper<RentalOrder>()
                .eq(RentalOrder::getDeleted, 0)
                .ne(RentalOrder::getOrderStatus, 6));
        Map<Long, Long> counts = new HashMap<>();
        for (RentalOrder order : orders) {
            counts.merge(order.getVehicleId(), 1L, Long::sum);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        counts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(8)
                .forEach(e -> {
                    Vehicle vehicle = vehicleMapper.selectById(e.getKey());
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", vehicle != null ? vehicle.getModel() : "车辆" + e.getKey());
                    item.put("value", e.getValue());
                    result.add(item);
                });
        return R.ok(result);
    }

    @GetMapping("/storeRank")
    public R<List<Map<String, Object>>> storeRank() {
        List<RentalOrder> orders = orderMapper.selectList(new LambdaQueryWrapper<RentalOrder>()
                .eq(RentalOrder::getDeleted, 0)
                .ne(RentalOrder::getOrderStatus, 6));
        Map<Long, Long> counts = new HashMap<>();
        for (RentalOrder order : orders) {
            counts.merge(order.getPickupStoreId(), 1L, Long::sum);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        counts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(8)
                .forEach(e -> {
                    var store = storeMapper.selectById(e.getKey());
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", store != null ? store.getStoreName() : "门店" + e.getKey());
                    item.put("value", e.getValue());
                    result.add(item);
                });
        return R.ok(result);
    }

    @GetMapping("/orderTrend")
    public R<Map<String, Object>> orderTrend() {
        List<String> months = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        List<BigDecimal> revenues = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime start = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime end = start.plusMonths(1);

            months.add(start.getMonthValue() + "月");

            counts.add(orderMapper.selectCount(new LambdaQueryWrapper<RentalOrder>()
                    .ge(RentalOrder::getCreateTime, start)
                    .lt(RentalOrder::getCreateTime, end)
                    .eq(RentalOrder::getDeleted, 0)));

            List<RentalOrder> orders = orderMapper.selectList(new LambdaQueryWrapper<RentalOrder>()
                    .ge(RentalOrder::getCreateTime, start)
                    .lt(RentalOrder::getCreateTime, end)
                    .eq(RentalOrder::getOrderStatus, 5)
                    .isNotNull(RentalOrder::getPaidAmount)
                    .eq(RentalOrder::getDeleted, 0));
            revenues.add(orders.stream()
                    .map(RentalOrder::getPaidAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("months", months);
        data.put("orderCounts", counts);
        data.put("revenues", revenues);
        return R.ok(data);
    }
}