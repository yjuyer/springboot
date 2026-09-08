package com.evrental.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evrental.business.entity.ChargingRecord;
import com.evrental.business.entity.Vehicle;
import com.evrental.business.entity.VehicleDispatch;
import com.evrental.business.entity.VehicleRepair;
import com.evrental.business.mapper.ChargingRecordMapper;
import com.evrental.business.mapper.VehicleDispatchMapper;
import com.evrental.business.mapper.VehicleMapper;
import com.evrental.business.mapper.VehicleRepairMapper;
import com.evrental.common.enums.VehicleStatusEnum;
import com.evrental.common.exception.BusinessException;
import com.evrental.common.result.R;
import com.evrental.common.security.LoginUser;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/** 管理员 - 调度/维修/充电管理 */
@RestController
@RequestMapping("/api/admin/operation")
@RequiredArgsConstructor
public class AdminOperationController {

    private final VehicleMapper vehicleMapper;
    private final VehicleDispatchMapper dispatchMapper;
    private final VehicleRepairMapper repairMapper;
    private final ChargingRecordMapper chargingMapper;

    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("dispatching", vehicleMapper.selectCount(new LambdaQueryWrapper<Vehicle>()
                .eq(Vehicle::getVehicleStatus, VehicleStatusEnum.DISPATCHING.getCode()).eq(Vehicle::getDeleted, 0)));
        data.put("repairing", vehicleMapper.selectCount(new LambdaQueryWrapper<Vehicle>()
                .eq(Vehicle::getVehicleStatus, VehicleStatusEnum.REPAIRING.getCode()).eq(Vehicle::getDeleted, 0)));
        data.put("charging", vehicleMapper.selectCount(new LambdaQueryWrapper<Vehicle>()
                .eq(Vehicle::getVehicleStatus, VehicleStatusEnum.CHARGING.getCode()).eq(Vehicle::getDeleted, 0)));
        data.put("lowBattery", vehicleMapper.selectCount(new LambdaQueryWrapper<Vehicle>()
                .lt(Vehicle::getCurrentBattery, 30).eq(Vehicle::getDeleted, 0)));
        return R.ok(data);
    }

    @GetMapping("/dispatch/list")
    public R<IPage<VehicleDispatch>> dispatchList(@RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                  @RequestParam(required = false) Integer status) {
        return R.ok(dispatchMapper.selectDispatchPage(new Page<>(pageNum, pageSize), status));
    }

    @PostMapping("/dispatch/create")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> createDispatch(@AuthenticationPrincipal LoginUser user, @RequestBody VehicleDispatch req) {
        Vehicle vehicle = vehicleMapper.selectById(req.getVehicleId());
        if (vehicle == null) throw new BusinessException("车辆不存在");
        if (vehicle.getVehicleStatus() != VehicleStatusEnum.IDLE.getCode()) {
            throw new BusinessException("仅空闲车辆可以调度");
        }
        req.setFromStoreId(vehicle.getStoreId());
        req.setStatus(1);
        req.setOperatorId(user != null ? user.getUserId() : null);
        req.setStartTime(LocalDateTime.now());
        dispatchMapper.insert(req);
        vehicleMapper.update(null, new LambdaUpdateWrapper<Vehicle>()
                .eq(Vehicle::getId, req.getVehicleId())
                .set(Vehicle::getVehicleStatus, VehicleStatusEnum.DISPATCHING.getCode()));
        return R.ok();
    }

    @PostMapping("/dispatch/complete/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> completeDispatch(@PathVariable Long id) {
        VehicleDispatch dispatch = dispatchMapper.selectById(id);
        if (dispatch == null) throw new BusinessException("调度记录不存在");
        dispatch.setStatus(2);
        dispatch.setCompleteTime(LocalDateTime.now());
        dispatchMapper.updateById(dispatch);
        vehicleMapper.update(null, new LambdaUpdateWrapper<Vehicle>()
                .eq(Vehicle::getId, dispatch.getVehicleId())
                .set(Vehicle::getStoreId, dispatch.getToStoreId())
                .set(Vehicle::getVehicleStatus, VehicleStatusEnum.IDLE.getCode()));
        return R.ok();
    }

    @GetMapping("/repair/list")
    public R<IPage<VehicleRepair>> repairList(@RequestParam(defaultValue = "1") int pageNum,
                                              @RequestParam(defaultValue = "10") int pageSize,
                                              @RequestParam(required = false) Integer status) {
        return R.ok(repairMapper.selectRepairPage(new Page<>(pageNum, pageSize), status));
    }

    @PostMapping("/repair/create")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> createRepair(@AuthenticationPrincipal LoginUser user, @RequestBody VehicleRepair req) {
        Vehicle vehicle = vehicleMapper.selectById(req.getVehicleId());
        if (vehicle == null) throw new BusinessException("车辆不存在");
        if (vehicle.getVehicleStatus() == VehicleStatusEnum.RENTING.getCode()) {
            throw new BusinessException("租赁中的车辆不能报修");
        }
        req.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        req.setStartTime(req.getStartTime() == null ? LocalDateTime.now() : req.getStartTime());
        req.setOperatorId(user != null ? user.getUserId() : null);
        repairMapper.insert(req);
        vehicleMapper.update(null, new LambdaUpdateWrapper<Vehicle>()
                .eq(Vehicle::getId, req.getVehicleId())
                .set(Vehicle::getVehicleStatus, VehicleStatusEnum.REPAIRING.getCode()));
        return R.ok();
    }

    @PostMapping("/repair/complete/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> completeRepair(@PathVariable Long id, @RequestBody(required = false) CompleteRepairReq req) {
        VehicleRepair repair = repairMapper.selectById(id);
        if (repair == null) throw new BusinessException("维修记录不存在");
        repair.setStatus(2);
        repair.setEndTime(LocalDateTime.now());
        if (req != null) {
            repair.setCost(req.getCost());
            repair.setRemark(req.getRemark());
        }
        repairMapper.updateById(repair);
        vehicleMapper.update(null, new LambdaUpdateWrapper<Vehicle>()
                .eq(Vehicle::getId, repair.getVehicleId())
                .set(Vehicle::getVehicleStatus, VehicleStatusEnum.IDLE.getCode()));
        return R.ok();
    }

    @GetMapping("/charging/list")
    public R<IPage<ChargingRecord>> chargingList(@RequestParam(defaultValue = "1") int pageNum,
                                                 @RequestParam(defaultValue = "10") int pageSize,
                                                 @RequestParam(required = false) Integer status) {
        return R.ok(chargingMapper.selectChargingPage(new Page<>(pageNum, pageSize), status));
    }

    @PostMapping("/charging/start")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> startCharging(@AuthenticationPrincipal LoginUser user, @RequestBody ChargingRecord req) {
        Vehicle vehicle = vehicleMapper.selectById(req.getVehicleId());
        if (vehicle == null) throw new BusinessException("车辆不存在");
        if (vehicle.getVehicleStatus() == VehicleStatusEnum.RENTING.getCode()) {
            throw new BusinessException("租赁中的车辆不能充电");
        }
        req.setStartBattery(req.getStartBattery() == null ? vehicle.getCurrentBattery() : req.getStartBattery());
        req.setStartTime(req.getStartTime() == null ? LocalDateTime.now() : req.getStartTime());
        req.setStatus(0);
        req.setOperatorId(user != null ? user.getUserId() : null);
        chargingMapper.insert(req);
        vehicleMapper.update(null, new LambdaUpdateWrapper<Vehicle>()
                .eq(Vehicle::getId, req.getVehicleId())
                .set(Vehicle::getVehicleStatus, VehicleStatusEnum.CHARGING.getCode())
                .set(Vehicle::getChargingStatus, 1));
        return R.ok();
    }

    @PostMapping("/charging/complete/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> completeCharging(@PathVariable Long id, @RequestBody CompleteChargingReq req) {
        ChargingRecord record = chargingMapper.selectById(id);
        if (record == null) throw new BusinessException("充电记录不存在");
        record.setEndBattery(req.getEndBattery());
        record.setCost(req.getCost());
        record.setEndTime(LocalDateTime.now());
        record.setStatus(1);
        chargingMapper.updateById(record);
        vehicleMapper.update(null, new LambdaUpdateWrapper<Vehicle>()
                .eq(Vehicle::getId, record.getVehicleId())
                .set(Vehicle::getCurrentBattery, req.getEndBattery())
                .set(Vehicle::getChargingStatus, req.getEndBattery() != null && req.getEndBattery() >= 100 ? 2 : 0)
                .set(Vehicle::getVehicleStatus, VehicleStatusEnum.IDLE.getCode()));
        return R.ok();
    }

    @Data
    static class CompleteRepairReq {
        private BigDecimal cost;
        private String remark;
    }

    @Data
    static class CompleteChargingReq {
        private Integer endBattery;
        private BigDecimal cost;
    }
}
