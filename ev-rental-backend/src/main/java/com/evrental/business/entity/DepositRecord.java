package com.evrental.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 押金记录表 (deposit_record)
 */
@Data
@TableName("deposit_record")
public class DepositRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long vehicleId;
    private BigDecimal amount;
    /** 0待支付 1押金冻结 2押金已退 */
    private Integer status;
    private LocalDateTime freezeTime;
    private LocalDateTime refundTime;

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
    private String phone;
    @TableField(exist = false)
    private String vehicleInfo;
}