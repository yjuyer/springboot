package com.evrental.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("image_blob")
public class ImageBlob {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private byte[] data;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
