USE `nev_rental`;

ALTER TABLE `rental_order`
    ADD COLUMN IF NOT EXISTS `coupon_id` BIGINT DEFAULT NULL COMMENT '使用的优惠券ID' AFTER `remark`,
    ADD COLUMN IF NOT EXISTS `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额（元）' AFTER `coupon_id`;

CREATE TABLE IF NOT EXISTS `vehicle_review` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `order_id`    BIGINT       NOT NULL                COMMENT '订单ID',
    `user_id`     BIGINT       NOT NULL                COMMENT '用户ID',
    `vehicle_id`  BIGINT       NOT NULL                COMMENT '车辆ID',
    `store_id`    BIGINT       DEFAULT NULL            COMMENT '评价门店ID',
    `rating`      TINYINT      NOT NULL                COMMENT '评分：1-5',
    `tags`        VARCHAR(200) DEFAULT NULL            COMMENT '评价标签，逗号分隔',
    `content`     VARCHAR(1000) DEFAULT NULL           COMMENT '评价内容',
    `anonymous`   TINYINT      NOT NULL DEFAULT 0      COMMENT '是否匿名：0-否 1-是',
    `status`      TINYINT      NOT NULL DEFAULT 0      COMMENT '状态：0-待审核 1-已通过 2-已驳回',
    `reply`       VARCHAR(500) DEFAULT NULL            COMMENT '管理员回复',
    `reply_time`  DATETIME     DEFAULT NULL            COMMENT '回复时间',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_review_order` (`order_id`),
    KEY `idx_vehicle_id` (`vehicle_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='车辆评价表';

CREATE TABLE IF NOT EXISTS `vehicle_dispatch` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '调度ID',
    `vehicle_id`    BIGINT       NOT NULL                COMMENT '车辆ID',
    `from_store_id` BIGINT       NOT NULL                COMMENT '原门店ID',
    `to_store_id`   BIGINT       NOT NULL                COMMENT '目标门店ID',
    `reason`        VARCHAR(300) DEFAULT NULL            COMMENT '调度原因',
    `status`        TINYINT      NOT NULL DEFAULT 0      COMMENT '状态：0-待调度 1-调度中 2-已完成 3-已取消',
    `operator_id`   BIGINT       DEFAULT NULL            COMMENT '操作人ID',
    `start_time`    DATETIME     DEFAULT NULL            COMMENT '开始时间',
    `complete_time` DATETIME     DEFAULT NULL            COMMENT '完成时间',
    `remark`        VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_vehicle_id` (`vehicle_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='车辆调度记录表';
