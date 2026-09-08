package com.evrental.business.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evrental.business.dto.VehicleDetailVO;
import com.evrental.business.dto.VehicleHotVO;
import com.evrental.business.dto.VehicleQueryDTO;
import com.evrental.business.entity.Vehicle;
import com.evrental.business.entity.VehicleImage;
import com.evrental.business.mapper.StoreMapper;
import com.evrental.business.mapper.VehicleMapper;
import com.evrental.business.service.VehicleImageService;
import com.evrental.business.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl extends ServiceImpl<VehicleMapper, Vehicle> implements VehicleService {

    private final VehicleImageService vehicleImageService;
    private final StoreMapper storeMapper;

    @Override
    public IPage<Vehicle> pageVehicles(VehicleQueryDTO query) {
        Page<Vehicle> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<Vehicle> result = baseMapper.selectVehiclePage(page, query);
        for (Vehicle v : result.getRecords()) {
            if (v.getImage() == null || v.getImage().isEmpty()) {
                v.setImage(v.getMainImageUrl());
            }
        }
        return result;
    }

    @Override
    public List<VehicleHotVO> getHotVehicles() {
        List<Vehicle> vehicles = baseMapper.selectHotVehicles();
        return vehicles.stream().map(v -> {
            VehicleHotVO vo = new VehicleHotVO();
            vo.setId(v.getId());
            vo.setVehicleName(v.getModel());
            vo.setCoverImage(v.getImage() != null ? v.getImage() : v.getMainImageUrl());
            vo.setBatteryRange(v.getRangeKm());
            vo.setBatteryLevel(v.getChargingStatus() != null ? 100 - (v.getChargingStatus() * 20) : 95);
            vo.setDailyPrice(v.getDailyPrice());
            vo.setStatus(v.getVehicleStatus());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public VehicleDetailVO getVehicleDetail(Long id) {
        Vehicle vehicle = getById(id);
        if (vehicle == null) {
            throw new RuntimeException("车辆不存在");
        }

        if (vehicle.getImage() == null || vehicle.getImage().isEmpty()) {
            vehicle.setImage(vehicle.getMainImageUrl());
        }

        // 查询门店信息
        String storeName = null;
        String storeAddress = null;
        String storePhone = null;
        BigDecimal storeLongitude = null;
        BigDecimal storeLatitude = null;

        if (vehicle.getStoreId() != null) {
            var store = storeMapper.selectById(vehicle.getStoreId());
            if (store != null) {
                storeName = store.getStoreName();
                storeAddress = store.getAddress();
                storePhone = store.getPhone();
                storeLongitude = store.getLongitude();
                storeLatitude = store.getLatitude();
            }
        }

        VehicleDetailVO vo = new VehicleDetailVO();
        BeanUtils.copyProperties(vehicle, vo);
        vo.setVehicleName(vehicle.getModel());
        vo.setCoverImage(vehicle.getImage());
        vo.setBatteryRange(vehicle.getRangeKm());
        vo.setBatteryLevel(vehicle.getCurrentBattery() != null ? vehicle.getCurrentBattery() : 0);
        vo.setDailyPrice(vehicle.getDailyPrice());
        vo.setStatus(vehicle.getVehicleStatus());
        vo.setStoreName(storeName);
        vo.setStoreAddress(storeAddress);
        vo.setStorePhone(storePhone);
        vo.setStoreLongitude(storeLongitude);
        vo.setStoreLatitude(storeLatitude);

        List<VehicleImage> images = vehicleImageService.getByVehicleId(id);
        vo.setImages(images);

        return vo;
    }
}
