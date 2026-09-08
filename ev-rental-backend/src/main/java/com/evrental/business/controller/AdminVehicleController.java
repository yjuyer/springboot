package com.evrental.business.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evrental.business.dto.VehicleQueryDTO;
import com.evrental.business.entity.Vehicle;
import com.evrental.business.entity.VehicleImage;
import com.evrental.business.service.VehicleImageService;
import com.evrental.business.service.VehicleService;
import com.evrental.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vehicle")
@RequiredArgsConstructor
public class AdminVehicleController {

    private final VehicleService vehicleService;
    private final VehicleImageService vehicleImageService;

    @GetMapping("/list")
    public R<IPage<Vehicle>> list(VehicleQueryDTO query) {
        return R.ok(vehicleService.pageVehicles(query));
    }

    @PostMapping("/add")
    public R<Void> add(@RequestBody Vehicle vehicle) {
        vehicleService.save(vehicle);
        return R.ok();
    }

    @PutMapping("/update")
    public R<Void> update(@RequestBody Vehicle vehicle) {
        vehicleService.updateById(vehicle);
        return R.ok();
    }

    @DeleteMapping("/delete/{id}")
    public R<Void> delete(@PathVariable Long id) {
        vehicleImageService.deleteByVehicleId(id);
        vehicleService.removeById(id);
        return R.ok();
    }

    @GetMapping("/images/{vehicleId}")
    public R<List<VehicleImage>> getImages(@PathVariable Long vehicleId) {
        return R.ok(vehicleImageService.getByVehicleId(vehicleId));
    }

    @PostMapping("/images/save")
    public R<Void> saveImages(@RequestParam Long vehicleId, @RequestBody List<VehicleImage> images) {
        vehicleImageService.saveImages(vehicleId, images);
        return R.ok();
    }

    @DeleteMapping("/images/delete/{imageId}")
    public R<Void> deleteImage(@PathVariable Long imageId) {
        vehicleImageService.removeById(imageId);
        return R.ok();
    }

    /**
     * 批量更新车辆图片
     */
    @PostMapping("/batchUpdateImages")
    public R<Void> batchUpdateImages() {
        String[][] data = {
            {"汉EV", "/images/byd-hanev.png"},
            {"唐EV", "/images/byd-tangev.png"},
            {"海豹", "/images/byd-hanev.png"},
            {"秦PLUS EV", "/images/byd-PLUS-ev.png"},
            {"宋PLUS EV", "/images/byd-tangev.png"},
            {"元PLUS", "/images/byd-PLUS-ev.png"},
            {"Model 3", "/images/tesla-m3.png"},
            {"Model Y", "/images/tesla-my.png"},
            {"Model S", "/images/tesla-m3.png"},
            {"Model X", "/images/tesla-my.png"},
            {"ES6", "/images/nio-es6.png"},
            {"ET5", "/images/nio-et5.png"},
            {"ES8", "/images/nio-es6.png"},
            {"ET7", "/images/nio-et5.png"},
            {"P7", "/images/xp-p7.png"},
            {"G9", "/images/xp-g9.png"},
            {"P5", "/images/xp-p7.png"},
            {"G6", "/images/xp-g9.png"},
            {"L7", "/images/li-l7.png"},
            {"L9", "/images/li-l7.png"},
            {"MEGA", "/images/li-l7.png"},
            {"L6", "/images/li-l7.png"},
            {"M5 EV", "/images/nio-es6.png"},
            {"M7", "/images/nio-et5.png"},
            {"M9", "/images/nio-es6.png"},
            {"M5 增程", "/images/nio-et5.png"},
            {"001", "/images/xp-g9.png"},
            {"007", "/images/xp-p7.png"},
            {"009", "/images/xp-g9.png"},
            {"X", "/images/xp-p7.png"},
            {"C11", "/images/li-l7.png"},
            {"C01", "/images/byd-hanev.png"},
            {"C10", "/images/byd-tangev.png"},
            {"S01", "/images/byd-PLUS-ev.png"},
        };

        for (String[] item : data) {
            vehicleService.update(new LambdaUpdateWrapper<Vehicle>()
                    .eq(Vehicle::getModel, item[0])
                    .set(Vehicle::getImage, item[1]));
        }

        return R.ok();
    }
}
