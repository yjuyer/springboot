package com.evrental.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 门店表 (store)
 * 字段与数据库列一一对应
 */
@Data
@TableName("store")
public class Store implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String storeName;
    private String address;
    private String city;
    private String province;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String phone;
    private String managerName;
    private String businessHours;
    private String description;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
