package com.evrental.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evrental.business.dto.VehicleDetailVO;
import com.evrental.business.dto.VehicleHotVO;
import com.evrental.business.dto.VehicleQueryDTO;
import com.evrental.business.entity.RentalOrder;
import com.evrental.business.entity.Vehicle;
import com.evrental.business.mapper.RentalOrderMapper;
import com.evrental.business.service.VehicleService;
import com.evrental.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicle")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final RentalOrderMapper rentalOrderMapper;

    @GetMapping("/list")
    public R<IPage<Vehicle>> list(VehicleQueryDTO query) {
        return R.ok(vehicleService.pageVehicles(query));
    }

    @GetMapping("/hot")
    public R<List<VehicleHotVO>> hot() {
        return R.ok(vehicleService.getHotVehicles());
    }

    @GetMapping("/detail/{id}")
    public R<VehicleDetailVO> detail(@PathVariable Long id) {
        return R.ok(vehicleService.getVehicleDetail(id));
    }

    @GetMapping("/{id}/calendar")
    public R<Map<String, Object>> calendar(@PathVariable Long id) {
        List<RentalOrder> orders = rentalOrderMapper.selectList(new LambdaQueryWrapper<RentalOrder>()
                .eq(RentalOrder::getVehicleId, id)
                .eq(RentalOrder::getDeleted, 0)
                .in(RentalOrder::getOrderStatus, 0, 1, 2, 3, 4, 5, 7)
                .ge(RentalOrder::getReturnTime, LocalDateTime.now().minusDays(1))
                .orderByAsc(RentalOrder::getPickupTime));
        List<String> bookedDates = new ArrayList<>();
        List<Map<String, Object>> ranges = new ArrayList<>();
        for (RentalOrder order : orders) {
            if (order.getPickupTime() == null || order.getReturnTime() == null) {
                continue;
            }
            LocalDate start = order.getPickupTime().toLocalDate();
            LocalDate end = order.getReturnTime().toLocalDate();
            Map<String, Object> range = new HashMap<>();
            range.put("orderId", order.getId());
            range.put("pickupTime", order.getPickupTime());
            range.put("returnTime", order.getReturnTime());
            range.put("status", order.getOrderStatus());
            ranges.add(range);
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                String value = d.toString();
                if (!bookedDates.contains(value)) {
                    bookedDates.add(value);
                }
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("bookedDates", bookedDates);
        data.put("ranges", ranges);
        return R.ok(data);
    }
}
