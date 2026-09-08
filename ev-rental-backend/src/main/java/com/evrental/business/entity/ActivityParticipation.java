package com.evrental.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动参与记录实体类
 * 对应数据库表：activity_participation
 */
@Data
@TableName("activity_participation")
public class ActivityParticipation implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 活动ID */
    private Long activityId;
    /** 用户ID */
    private Long userId;
    /** 关联订单ID */
    private Long orderId;
    /** 订单编号 */
    private String orderNo;
    /** 本单优惠金额 */
    private BigDecimal discountAmount;

    /** 参与时间 */
    private LocalDateTime createTime;
}
