package com.evrental.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会员积分记录实体类
 * 对应数据库表：point_record
 */
@Data
@TableName("point_record")
public class PointRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 积分变动（正数=获得，负数=消费） */
    private Integer points;
    /** 类型：1-订单完成 2-积分兑换 3-活动赠送 4-管理员调整 */
    private Integer type;
    /** 备注说明 */
    private String remark;
    /** 关联订单ID */
    private Long orderId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
