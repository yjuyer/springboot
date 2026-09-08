package com.evrental.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evrental.business.entity.VehicleImage;
import java.util.List;

public interface VehicleImageService extends IService<VehicleImage> {
    List<VehicleImage> getByVehicleId(Long vehicleId);
    void saveImages(Long vehicleId, List<VehicleImage> images);
    void deleteByVehicleId(Long vehicleId);
}