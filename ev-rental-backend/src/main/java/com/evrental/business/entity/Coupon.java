package com.evrental.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实体类
 *
 * <p>对应数据库表：coupon</p>
 *
 * <p>字段说明：</p>
 * <ul>
 *   <li>id - 优惠券ID（主键，自增）</li>
 *   <li>couponName - 优惠券名称</li>
 *   <li>couponType - 类型：1-满减券 2-折扣券 3-立减券</li>
 *   <li>discountValue - 优惠值（满减金额/折扣率/立减金额）</li>
 *   <li>minAmount - 最低消费金额</li>
 *   <li>startTime - 有效期开始时间</li>
 *   <li>endTime - 有效期结束时间</li>
 *   <li>totalCount - 发放总量（0表示不限量）</li>
 *   <li>usedCount - 已领取数量</li>
 *   <li>status - 状态：0-禁用 1-启用</li>
 *   <li>description - 优惠券说明</li>
 * </ul>
 */
@Data
@TableName("coupon")
public class Coupon implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String couponName;
    /** 类型：1-满减券 2-折扣券 3-立减券 */
    private Integer couponType;
    /** 优惠值（满减金额/折扣率/立减金额） */
    private BigDecimal discountValue;
    /** 最低消费金额 */
    private BigDecimal minAmount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    /** 发放总量（0表示不限） */
    private Integer totalCount;
    /** 已领取数量 */
    private Integer usedCount;
    /** 状态：0-禁用 1-启用 */
    private Integer status;
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
