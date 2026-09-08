package com.evrental.business.dto;

import lombok.Data;

/**
 * 车辆查询DTO
 */
@Data
public class VehicleQueryDTO {
    private Long brandId;
    private String vehicleType;
    private Integer minPrice;
    private Integer maxPrice;
    private Integer minRange;
    private Integer maxRange;
    private Long storeId;
    private Integer vehicleStatus;
    private String keyword;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
