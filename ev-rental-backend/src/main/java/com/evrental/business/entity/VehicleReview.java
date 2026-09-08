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

/** 车辆评价表 */
@Data
@TableName("vehicle_review")
public class VehicleReview implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long userId;
    private Long vehicleId;
    private Long storeId;
    private Integer rating;
    private String tags;
    private String content;
    private Integer anonymous;
    /** 0待审核 1已通过 2已驳回 */
    private Integer status;
    private String reply;
    private LocalDateTime replyTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    @TableField(exist = false)
    private String username;
    @TableField(exist = false)
    private String vehicleModel;
    @TableField(exist = false)
    private String storeName;
}
