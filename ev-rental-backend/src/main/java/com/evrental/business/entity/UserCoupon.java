package com.evrental.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券实体类
 *
 * <p>对应数据库表：user_coupon</p>
 *
 * <p>字段说明：</p>
 * <ul>
 *   <li>id - 主键ID（自增）</li>
 *   <li>userId - 用户ID</li>
 *   <li>couponId - 优惠券ID</li>
 *   <li>orderId - 使用的订单ID（NULL表示未使用）</li>
 *   <li>status - 状态：0-未使用 1-已使用 2-已过期</li>
 *   <li>getTime - 领取时间</li>
 *   <li>useTime - 使用时间</li>
 * </ul>
 *
 * <p>非数据库字段（关联查询优惠券信息）：</p>
 * <ul>
 *   <li>couponName - 优惠券名称</li>
 *   <li>couponType - 优惠券类型</li>
 *   <li>discountValue - 优惠值</li>
 *   <li>minAmount - 最低消费金额</li>
 *   <li>startTime/endTime - 有效期</li>
 *   <li>description - 优惠券说明</li>
 * </ul>
 */
@Data
@TableName("user_coupon")
public class UserCoupon implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long couponId;
    /** 使用的订单ID（NULL表示未使用） */
    private Long orderId;
    /** 状态：0-未使用 1-已使用 2-已过期 */
    private Integer status;
    private LocalDateTime getTime;
    private LocalDateTime useTime;
    /** 过期时间（领取后30天） */
    private LocalDateTime expireTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    // ===== 非数据库字段 =====
    @TableField(exist = false)
    private String couponName;
    @TableField(exist = false)
    private Integer couponType;
    @TableField(exist = false)
    private BigDecimal discountValue;
    @TableField(exist = false)
    private BigDecimal minAmount;
    @TableField(exist = false)
    private LocalDateTime startTime;
    @TableField(exist = false)
    private LocalDateTime endTime;
    @TableField(exist = false)
    private String description;
}
