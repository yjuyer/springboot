package com.evrental.business.dto;

import com.evrental.business.entity.VehicleImage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.evrental.business.entity.Vehicle;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleDetailVO extends VehicleHotVO {
    private String model;
    private String licensePlate;
    private String color;
    private Integer seatCount;
    private BigDecimal batteryCapacity;
    private BigDecimal fastCharge;
    private BigDecimal mileage;
    private BigDecimal deposit;
    private Long storeId;
    private String storeName;
    private String storeAddress;
    private String storePhone;
    private BigDecimal storeLongitude;
    private BigDecimal storeLatitude;
    private String description;
    private String mainImageUrl;
    private List<VehicleImage> images;
}