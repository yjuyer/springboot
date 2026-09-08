package com.evrental.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 公告表 (notice) */
@Data
@TableName("notice")
public class Notice implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    /** 类型：notice-公告 announcement-通知 */
    private String type;
    private Long publisherId;
    private Integer isTop;
    private Integer status;
    private LocalDateTime publishTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
