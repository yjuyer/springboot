-- ===================== 优惠券模块 ========================

SET NAMES utf8mb4;
USE `nev_rental`;

-- -----------------------------------------------------------
-- 优惠券表 coupon
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
    `coupon_name`     VARCHAR(100)  NOT NULL                COMMENT '优惠券名称',
    `coupon_type`     TINYINT       NOT NULL DEFAULT 1      COMMENT '类型：1-满减券 2-折扣券 3-立减券',
    `discount_value`  DECIMAL(10,2) NOT NULL                COMMENT '优惠值（满减金额/折扣率/立减金额）',
    `min_amount`      DECIMAL(10,2) NOT NULL DEFAULT 0.00   COMMENT '最低消费金额',
    `start_time`      DATETIME      NOT NULL                COMMENT '有效期开始',
    `end_time`        DATETIME      NOT NULL                COMMENT '有效期结束',
    `total_count`     INT           NOT NULL DEFAULT 0      COMMENT '发放总量（0表示不限）',
    `used_count`      INT           NOT NULL DEFAULT 0      COMMENT '已领取数量',
    `status`          TINYINT       NOT NULL DEFAULT 1      COMMENT '状态：0-禁用 1-启用',
    `description`     VARCHAR(500)  DEFAULT NULL            COMMENT '优惠券说明',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='优惠券表';

-- -----------------------------------------------------------
-- 用户优惠券表 user_coupon
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT   NOT NULL                COMMENT '用户ID',
    `coupon_id`   BIGINT   NOT NULL                COMMENT '优惠券ID',
    `order_id`    BIGINT   DEFAULT NULL            COMMENT '使用的订单ID（NULL表示未使用）',
    `status`      TINYINT  NOT NULL DEFAULT 0      COMMENT '状态：0-未使用 1-已使用 2-已过期',
    `get_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    `use_time`    DATETIME DEFAULT NULL            COMMENT '使用时间',
    `expire_time` DATETIME DEFAULT NULL            COMMENT '过期时间（领取后30天）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT  NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_coupon_id` (`coupon_id`),
    KEY `idx_status` (`status`),
    UNIQUE KEY `uk_user_coupon` (`user_id`, `coupon_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户优惠券表';

-- -----------------------------------------------------------
-- 插入测试优惠券数据
-- -----------------------------------------------------------
INSERT INTO `coupon` (`coupon_name`, `coupon_type`, `discount_value`, `min_amount`, `start_time`, `end_time`, `total_count`, `status`, `description`) VALUES
('新人专享满300减50', 1, 50.00, 300.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1000, 1, '新用户专享，订单满300元可减50元'),
('周末特惠8折券', 2, 0.80, 200.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 500, 1, '周末租车享受8折优惠，最低消费200元'),
('立减30元优惠券', 3, 30.00, 100.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 2000, 1, '无门槛立减30元，订单满100元可用'),
('豪华车型专享100元券', 1, 100.00, 500.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 200, 1, '豪华车型专享，订单满500元减100元'),
('长租7天9折券', 2, 0.90, 0.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 300, 1, '租赁7天及以上享受9折优惠');
