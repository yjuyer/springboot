package com.evrental.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 电子发票实体类
 * 对应数据库表：invoice
 */
@Data
@TableName("invoice")
public class Invoice implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 关联订单ID */
    private Long orderId;
    /** 关联订单编号 */
    private String orderNo;
    /** 申请用户ID */
    private Long userId;
    /** 发票类型：1-增值税普通发票 2-增值税专用发票 */
    private Integer invoiceType;
    /** 发票抬头 */
    private String invoiceTitle;
    /** 税号（企业必填） */
    private String taxNumber;
    /** 接收邮箱 */
    private String email;
    /** 发票金额 */
    private BigDecimal amount;
    /** 状态：0-待审核 1-已开具 2-已发送 3-已驳回 */
    private Integer status;
    /** 发票号码 */
    private String invoiceNo;
    /** 发票文件地址 */
    private String invoiceFileUrl;
    /** 驳回原因 */
    private String rejectReason;
    /** 申请时间 */
    private LocalDateTime applyTime;
    /** 审核时间 */
    private LocalDateTime auditTime;
    /** 发送时间 */
    private LocalDateTime sendTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    // ===== 非数据库字段 =====
    @TableField(exist = false)
    private String username;
    @TableField(exist = false)
    private String phone;
    @TableField(exist = false)
    private String vehicleModel;
}
