package com.evrental.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 促销活动实体类
 * 对应数据库表：promotion_activity
 */
@Data
@TableName("promotion_activity")
public class PromotionActivity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 活动名称 */
    private String activityName;
    /** 活动类型：1-满减 2-折扣 3-立减 */
    private Integer activityType;
    /** 规则配置JSON */
    private String ruleConfig;
    /** 活动开始时间 */
    private LocalDateTime startTime;
    /** 活动结束时间 */
    private LocalDateTime endTime;
    /** 每人限参与次数（0表示不限） */
    private Integer userLimit;
    /** 活动总参与次数限制（0表示不限） */
    private Integer totalLimit;
    /** 已参与次数 */
    private Integer usedCount;
    /** 是否可与优惠券叠加：0-不可 1-可叠加 */
    private Integer stackable;
    /** 叠加时最高优惠金额 */
    private BigDecimal maxDiscount;
    /** 状态：0-禁用 1-启用 */
    private Integer status;
    /** 活动说明 */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
