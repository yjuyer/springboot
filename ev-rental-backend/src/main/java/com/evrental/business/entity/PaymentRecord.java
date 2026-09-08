package com.evrental.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录表 (payment_record)
 * 字段与数据库列一一对应
 */
@Data
@TableName("payment_record")
public class PaymentRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private Integer payType;
    /** 支付状态: 0待支付 1成功 2失败 3退款 */
    private Integer payStatus;
    private String transactionNo;
    private LocalDateTime payTime;
    /** 支付超时时间 */
    private LocalDateTime expireTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
