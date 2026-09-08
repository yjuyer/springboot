package com.evrental.business.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 车辆调度记录表 */
@Data
@TableName("vehicle_dispatch")
public class VehicleDispatch implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long vehicleId;
    private Long fromStoreId;
    private Long toStoreId;
    private String reason;
    /** 0待调度 1调度中 2已完成 3已取消 */
    private Integer status;
    private Long operatorId;
    private LocalDateTime startTime;
    private LocalDateTime completeTime;
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
    @TableField(exist = false)
    private String fromStoreName;
    @TableField(exist = false)
    private String toStoreName;
}
