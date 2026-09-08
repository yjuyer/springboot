package com.evrental.business.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class VehicleHotVO {
    private Long id;
    private String vehicleName;
    private String image;
    private String coverImage;
    private Integer batteryRange;
    private Integer batteryLevel;
    private BigDecimal dailyPrice;
    private Integer status;
}