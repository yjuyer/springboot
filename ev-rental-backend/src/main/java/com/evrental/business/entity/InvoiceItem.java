package com.evrental.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发票明细实体类
 * 对应数据库表：invoice_item
 */
@Data
@TableName("invoice_item")
public class InvoiceItem implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 关联发票ID */
    private Long invoiceId;
    /** 项目名称（如：车辆租金） */
    private String itemName;
    /** 项目金额 */
    private BigDecimal itemAmount;
    /** 数量 */
    private Integer quantity;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
