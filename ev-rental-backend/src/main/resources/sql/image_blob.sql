-- 图片二进制存储表
CREATE TABLE IF NOT EXISTS `image_blob` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '图片ID',
    `file_name`   VARCHAR(200)  NOT NULL                COMMENT '原始文件名',
    `file_type`   VARCHAR(20)   NOT NULL                COMMENT '文件类型 image/jpeg',
    `file_size`   BIGINT        NOT NULL                COMMENT '文件大小(字节)',
    `data`        LONGBLOB      NOT NULL                COMMENT '图片二进制数据',
    `create_time` DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片存储表';
