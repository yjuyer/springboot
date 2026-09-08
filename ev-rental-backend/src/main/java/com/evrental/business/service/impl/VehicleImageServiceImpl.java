package com.evrental.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evrental.business.entity.VehicleImage;
import com.evrental.business.mapper.VehicleImageMapper;
import com.evrental.business.service.VehicleImageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleImageServiceImpl extends ServiceImpl<VehicleImageMapper, VehicleImage> implements VehicleImageService {

    @Override
    public List<VehicleImage> getByVehicleId(Long vehicleId) {
        return list(new LambdaQueryWrapper<VehicleImage>()
                .eq(VehicleImage::getVehicleId, vehicleId)
                .orderByAsc(VehicleImage::getSortNum));
    }

    @Override
    @Transactional
    public void saveImages(Long vehicleId, List<VehicleImage> images) {
        deleteByVehicleId(vehicleId);
        for (int i = 0; i < images.size(); i++) {
            VehicleImage img = images.get(i);
            img.setVehicleId(vehicleId);
            if (img.getSortNum() == null) {
                img.setSortNum(i + 1);
            }
            save(img);
        }
    }

    @Override
    public void deleteByVehicleId(Long vehicleId) {
        remove(new LambdaQueryWrapper<VehicleImage>()
                .eq(VehicleImage::getVehicleId, vehicleId));
    }
}