package com.evrental.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 车辆实体类
 *
 * <p>对应数据库表：vehicle</p>
 *
 * <p>主要字段：</p>
 * <ul>
 *   <li>id - 车辆ID（主键）</li>
 *   <li>brandId - 品牌ID（关联vehicle_brand表）</li>
 *   <li>model - 车型名称（如：汉EV、Model 3）</li>
 *   <li>vehicleType - 车辆类型（轿车/SUV/MPV/跑车）</li>
 *   <li>licensePlate - 车牌号</li>
 *   <li>dailyPrice - 日租金（元）</li>
 *   <li>deposit - 押金（元）</li>
 *   <li>rangeKm - 续航里程（km）</li>
 *   <li>currentBattery - 当前电量百分比（0-100）</li>
 *   <li>vehicleStatus - 车辆状态：0-空闲 1-已预约 2-租赁中 3-维修中 4-充电中</li>
 *   <li>storeId - 所属门店ID（关联store表）</li>
 * </ul>
 */
@Data
@TableName("vehicle")
public class Vehicle implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long brandId;
    private String model;
    private String vehicleType;
    private String licensePlate;
    private String color;
    private Integer seatCount;
    private BigDecimal batteryCapacity;
    private Integer rangeKm;
    @TableField(exist = false)
    private BigDecimal fastCharge;
    private Integer currentBattery;
    private Integer chargingStatus;
    private BigDecimal dailyPrice;
    private BigDecimal deposit;
    private BigDecimal mileage;
    private Integer vehicleStatus;
    private Long storeId;
    private String description;
    private String image;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    @TableField(exist = false)
    private String storeName;
    @TableField(exist = false)
    private String mainImageUrl;
    @TableField(exist = false)
    private List<VehicleImage> images;
}
