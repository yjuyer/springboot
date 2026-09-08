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

/** 维修记录表 */
@Data
@TableName("repair_record")
public class VehicleRepair implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long vehicleId;
    private String repairType;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal cost;
    /** 0待维修 1维修中 2已完成 */
    private Integer status;
    private Long operatorId;
    private String remark;

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
