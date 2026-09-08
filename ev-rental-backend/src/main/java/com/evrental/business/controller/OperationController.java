package com.evrental.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evrental.business.entity.*;
import com.evrental.business.mapper.*;
import com.evrental.business.service.VehicleService;
import com.evrental.common.enums.VehicleStatusEnum;
import com.evrental.common.result.R;
import com.evrental.common.security.LoginUser;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 运营端 - 车辆调度、充电、维修管理
 */
@RestController
@RequestMapping("/api/operation")
@RequiredArgsConstructor
public class OperationController {

    private final VehicleMapper vehicleMapper;
    private final VehicleService vehicleService;
    private final VehicleDispatchMapper dispatchMapper;
    private final ChargingRecordMapper chargingMapper;
    private final VehicleRepairMapper repairMapper;

    // ======================== 充电管理 ========================

    @GetMapping("/charging/lowBattery")
    public R<List<Vehicle>> lowBattery() {
        return R.ok(vehicleMapper.selectList(new LambdaQueryWrapper<Vehicle>()
                .lt(Vehicle::getCurrentBattery, 20)
                .eq(Vehicle::getDeleted, 0)
                .orderByAsc(Vehicle::getCurrentBattery)));
    }

    @PostMapping("/charging/start")
    public R<Void> startCharging(@RequestBody StartChargeReq req,
                                 @AuthenticationPrincipal LoginUser user) {
        // 更新车辆状态
        vehicleService.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Vehicle>()
                .eq(Vehicle::getId, req.getVehicleId())
                .set(Vehicle::getVehicleStatus, VehicleStatusEnum.CHARGING.getCode())
                .set(Vehicle::getChargingStatus, 1));

        // 创建充电记录
        ChargingRecord record = new ChargingRecord();
        record.setVehicleId(req.getVehicleId());
        record.setStationName(req.getStationName());
        // record.chargeType(req.getChargeType());
        record.setStartBattery(req.getStartBattery() != null ? req.getStartBattery().intValue() : null);
        record.setStartTime(LocalDateTime.now());
        record.setStatus(0);
        record.setOperatorId(user.getUserId());
        chargingMapper.insert(record);
        return R.ok();
    }

    @PostMapping("/charging/end/{id}")
    public R<Void> endCharging(@PathVariable Long id, @RequestBody EndChargeReq req) {
        ChargingRecord record = chargingMapper.selectById(id);
        record.setEndBattery(req.getEndBattery() != null ? req.getEndBattery().intValue() : null);
        record.setEndTime(LocalDateTime.now());
        record.setStatus(1);
        chargingMapper.updateById(record);

        // 恢复车辆状态
        vehicleService.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Vehicle>()
                .eq(Vehicle::getId, record.getVehicleId())
                .set(Vehicle::getVehicleStatus, VehicleStatusEnum.IDLE.getCode())
                .set(Vehicle::getCurrentBattery, req.getEndBattery())
                .set(Vehicle::getChargingStatus, 2));
        return R.ok();
    }

    // ======================== 维修管理 ========================

    @GetMapping("/repair/list")
    public R<IPage<VehicleRepair>> repairList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(repairMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<VehicleRepair>().orderByDesc(VehicleRepair::getCreateTime)));
    }

    @PostMapping("/repair/create")
    public R<Void> createRepair(@RequestBody VehicleRepair repair,
                                @AuthenticationPrincipal LoginUser user) {
        repair.setStatus(0);
        repair.setOperatorId(user.getUserId());
        repairMapper.insert(repair);

        vehicleService.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Vehicle>()
                .eq(Vehicle::getId, repair.getVehicleId())
                .set(Vehicle::getVehicleStatus, VehicleStatusEnum.REPAIRING.getCode()));
        return R.ok();
    }

    @PostMapping("/repair/complete/{id}")
    public R<Void> completeRepair(@PathVariable Long id) {
        VehicleRepair repair = repairMapper.selectById(id);
        repair.setStatus(2);
        // repair.endTime(LocalDateTime.now());
        repairMapper.updateById(repair);

        vehicleService.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Vehicle>()
                .eq(Vehicle::getId, repair.getVehicleId())
                .set(Vehicle::getVehicleStatus, VehicleStatusEnum.IDLE.getCode()));
        return R.ok();
    }

    // ======================== 调度管理 ========================

    @GetMapping("/dispatch/list")
    public R<IPage<VehicleDispatch>> dispatchList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(dispatchMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<VehicleDispatch>().orderByDesc(VehicleDispatch::getCreateTime)));
    }

    @PostMapping("/dispatch/create")
    public R<Void> createDispatch(@RequestBody VehicleDispatch dispatch,
                                  @AuthenticationPrincipal LoginUser user) {
        dispatch.setStatus(0);
        // dispatch.dispatchTime(LocalDateTime.now());
        dispatch.setOperatorId(user.getUserId());
        dispatchMapper.insert(dispatch);
        return R.ok();
    }

    @PostMapping("/dispatch/complete/{id}")
    public R<Void> completeDispatch(@PathVariable Long id) {
        VehicleDispatch d = dispatchMapper.selectById(id);
        d.setStatus(2);
        // d.actualArriveTime(LocalDateTime.now());
        dispatchMapper.updateById(d);

        // 更新车辆所在门店
        vehicleService.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Vehicle>()
                .eq(Vehicle::getId, d.getVehicleId())
                .set(Vehicle::getStoreId, d.getToStoreId())
                .set(Vehicle::getVehicleStatus, VehicleStatusEnum.IDLE.getCode()));
        return R.ok();
    }

    // ======================== 请求体 ========================

    @Data static class StartChargeReq {
        private Long vehicleId;
        private String stationName;
        private Integer chargeType;
        private BigDecimal startBattery;
    }
    @Data static class EndChargeReq {
        private BigDecimal endBattery;
    }
}
