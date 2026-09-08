package com.evrental.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户收藏实体类
 *
 * <p>对应数据库表：user_favorite</p>
 */
@Data
@TableName("user_favorite")
public class UserFavorite implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 车辆ID */
    private Long vehicleId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // ===== 非数据库字段（关联查询车辆信息） =====
    @TableField(exist = false)
    private String model;
    @TableField(exist = false)
    private String image;
    @TableField(exist = false)
    private java.math.BigDecimal dailyPrice;
    @TableField(exist = false)
    private Integer rangeKm;
    @TableField(exist = false)
    private Integer vehicleStatus;
}
