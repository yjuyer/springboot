package com.evrental.business.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.evrental.business.dto.VehicleDetailVO;
import com.evrental.business.dto.VehicleHotVO;
import com.evrental.business.dto.VehicleQueryDTO;
import com.evrental.business.entity.Vehicle;

import java.util.List;

public interface VehicleService extends IService<Vehicle> {

    IPage<Vehicle> pageVehicles(VehicleQueryDTO query);

    List<VehicleHotVO> getHotVehicles();

    VehicleDetailVO getVehicleDetail(Long id);
}
