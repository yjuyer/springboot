package com.evrental.business.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 信用记录表 */
@Data
@TableName("credit_record")
public class CreditLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String changeType;
    private Integer changeAmount;
    private Integer beforeScore;
    private Integer afterScore;
    private String reason;
    private Long orderId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
