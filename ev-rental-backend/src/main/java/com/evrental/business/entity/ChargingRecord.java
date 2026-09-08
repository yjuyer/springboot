package com.evrental.business.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 充电记录表 */
@Data
@TableName("charging_record")
public class ChargingRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long vehicleId;
    private String stationName;
    private Integer startBattery;
    private Integer endBattery;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal cost;
    /** 0充电中 1已完成 */
    private Integer status;
    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    @TableField(exist = false)
    private String vehicleModel;
}
