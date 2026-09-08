package com.evrental.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 租赁订单实体类
 *
 * <p>对应数据库表：rental_order</p>
 *
 * <p>主要字段：</p>
 * <ul>
 *   <li>id - 订单ID（主键）</li>
 *   <li>orderNo - 订单编号（雪花算法生成）</li>
 *   <li>userId - 用户ID</li>
 *   <li>vehicleId - 车辆ID</li>
 *   <li>pickupStoreId/returnStoreId - 取车/还车门店ID</li>
 *   <li>pickupTime/returnTime - 预约取车/还车时间</li>
 *   <li>actualReturnTime - 实际还车时间</li>
 *   <li>dailyPrice - 日租金</li>
 *   <li>rentalDays - 租赁天数</li>
 *   <li>totalAmount - 订单总金额</li>
 *   <li>depositAmount - 押金金额</li>
 *   <li>paidAmount - 实际支付金额</li>
 *   <li>orderStatus - 订单状态（0-8，共9种状态）</li>
 *   <li>couponId - 使用的优惠券ID</li>
 *   <li>discountAmount - 优惠金额</li>
 * </ul>
 *
 * <p>订单状态流转：</p>
 * <pre>
 * 待支付(0) → 已支付(1) → 待取车(2) → 租赁中(3) → 待还车(4) → 已完成(5)
 * </pre>
 */
@Data
@TableName("rental_order")
public class RentalOrder implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long vehicleId;
    private Long pickupStoreId;
    private Long returnStoreId;
    private LocalDateTime pickupTime;
    private LocalDateTime returnTime;
    private LocalDateTime actualReturnTime;
    private BigDecimal dailyPrice;
    private Integer rentalDays;
    private BigDecimal totalAmount;
    private BigDecimal depositAmount;
    private BigDecimal paidAmount;
    /** 0待付 1已付 2租赁中 3已完成 4已取消 5超时 6退款 */
    private Integer orderStatus;
    private String payType;
    private LocalDateTime payTime;
    private String cancelReason;
    private Integer pickupBattery;
    private Integer returnBattery;
    private BigDecimal pickupMileage;
    private BigDecimal returnMileage;
    private String remark;
    /** 使用的优惠券ID */
    private Long couponId;
    /** 优惠金额 */
    private BigDecimal discountAmount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    // ===== 非数据库字段 =====
    @TableField(exist = false)
    private String vehicleModel;
    @TableField(exist = false)
    private String mainImageUrl;
    @TableField(exist = false)
    private Integer vehicleStatus;
    @TableField(exist = false)
    private java.math.BigDecimal vehicleDailyPrice;
    @TableField(exist = false)
    private java.math.BigDecimal vehicleDeposit;
    @TableField(exist = false)
    private Integer rangeKm;
    @TableField(exist = false)
    private Integer seatCount;
    @TableField(exist = false)
    private String color;
    @TableField(exist = false)
    private String pickupStoreName;
    @TableField(exist = false)
    private String pickupStoreAddress;
    @TableField(exist = false)
    private String pickupStorePhone;
    @TableField(exist = false)
    private String returnStoreName;
    @TableField(exist = false)
    private String returnStoreAddress;
    @TableField(exist = false)
    private String returnStorePhone;
    @TableField(exist = false)
    private String username;
    @TableField(exist = false)
    private String phone;
}
