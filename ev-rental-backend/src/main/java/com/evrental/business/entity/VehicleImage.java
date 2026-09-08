package com.evrental.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("vehicle_image")
public class VehicleImage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long vehicleId;
    private String imageUrl;
    private Integer sortNum;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
