-- ====================== 用户通知表 ======================
CREATE TABLE IF NOT EXISTS notification (
    id           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    user_id      BIGINT       NOT NULL                 COMMENT '接收用户ID',
    title        VARCHAR(100) NOT NULL                 COMMENT '通知标题',
    content      VARCHAR(500) NOT NULL                 COMMENT '通知内容',
    type         TINYINT      NOT NULL DEFAULT 1       COMMENT '类型: 1订单 2认证 3系统 4优惠',
    related_id   BIGINT       DEFAULT NULL             COMMENT '关联ID(订单ID/认证ID等)',
    is_read      TINYINT      NOT NULL DEFAULT 0       COMMENT '已读状态: 0未读 1已读',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    PRIMARY KEY (id),
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_user_time (user_id, create_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户通知表';
