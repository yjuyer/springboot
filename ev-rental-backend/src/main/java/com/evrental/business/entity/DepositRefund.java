package com.evrental.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 押金退款表 (deposit_refund)
 */
@Data
@TableName("deposit_refund")
public class DepositRefund implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long depositId;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    /** 退款方式: 1原路退回 2线下转账 */
    private Integer refundType;
    private String refundReason;
    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    @TableField(exist = false)
    private String operatorName;
}