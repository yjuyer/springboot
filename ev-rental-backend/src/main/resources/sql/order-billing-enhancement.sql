-- ============================================================
-- 订单与计费增强模块 - 数据库表创建脚本
-- ============================================================

SET NAMES utf8mb4;
USE `nev_rental`;

-- -----------------------------------------------------------
-- 1. 扩展 payment_record 表，增加支付超时时间
-- -----------------------------------------------------------
ALTER TABLE `payment_record`
ADD COLUMN `expire_time` DATETIME DEFAULT NULL COMMENT '支付超时时间（超过此时间未支付则自动取消）' AFTER `pay_time`;

-- -----------------------------------------------------------
-- 2. 电子发票表 invoice
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `invoice`;
CREATE TABLE `invoice` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '发票ID',
    `order_id`         BIGINT        NOT NULL                COMMENT '关联订单ID',
    `order_no`         VARCHAR(32)   NOT NULL                COMMENT '关联订单编号',
    `user_id`          BIGINT        NOT NULL                COMMENT '申请用户ID',
    `invoice_type`     TINYINT       NOT NULL DEFAULT 1      COMMENT '发票类型：1-增值税普通发票 2-增值税专用发票',
    `invoice_title`    VARCHAR(100)  NOT NULL                COMMENT '发票抬头',
    `tax_number`       VARCHAR(50)   DEFAULT NULL            COMMENT '税号（企业必填）',
    `email`            VARCHAR(100)  NOT NULL                COMMENT '接收邮箱',
    `amount`           DECIMAL(10,2) NOT NULL                COMMENT '发票金额（租金金额，不含押金）',
    `status`           TINYINT       NOT NULL DEFAULT 0      COMMENT '发票状态：0-待审核 1-已开具 2-已发送 3-已驳回',
    `invoice_no`       VARCHAR(50)   DEFAULT NULL            COMMENT '发票号码（管理员录入）',
    `invoice_file_url` VARCHAR(500)  DEFAULT NULL            COMMENT '发票PDF/图片文件地址',
    `reject_reason`    VARCHAR(500)  DEFAULT NULL            COMMENT '驳回原因',
    `apply_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `audit_time`       DATETIME      DEFAULT NULL            COMMENT '审核时间',
    `send_time`        DATETIME      DEFAULT NULL            COMMENT '发送时间',
    `create_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='电子发票表';

-- -----------------------------------------------------------
-- 3. 发票明细表 invoice_item
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `invoice_item`;
CREATE TABLE `invoice_item` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `invoice_id`  BIGINT        NOT NULL                COMMENT '关联发票ID',
    `item_name`   VARCHAR(100)  NOT NULL                COMMENT '项目名称（如：车辆租金、押金）',
    `item_amount` DECIMAL(10,2) NOT NULL                COMMENT '项目金额',
    `quantity`    INT           NOT NULL DEFAULT 1      COMMENT '数量',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_invoice_id` (`invoice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='发票明细表';

-- -----------------------------------------------------------
-- 4. 促销活动表 promotion_activity
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `promotion_activity`;
CREATE TABLE `promotion_activity` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    `activity_name`  VARCHAR(100)  NOT NULL                COMMENT '活动名称',
    `activity_type`  TINYINT       NOT NULL DEFAULT 1      COMMENT '活动类型：1-满减 2-折扣 3-立减',
    `rule_config`    TEXT          NOT NULL                COMMENT '规则配置JSON（如：{"minAmount":500,"discountAmount":100}或{"discountRate":0.85}）',
    `start_time`     DATETIME      NOT NULL                COMMENT '活动开始时间',
    `end_time`       DATETIME      NOT NULL                COMMENT '活动结束时间',
    `user_limit`     INT           NOT NULL DEFAULT 0      COMMENT '每人限参与次数（0表示不限）',
    `total_limit`    INT           NOT NULL DEFAULT 0      COMMENT '活动总参与次数限制（0表示不限）',
    `used_count`     INT           NOT NULL DEFAULT 0      COMMENT '已参与次数',
    `stackable`      TINYINT       NOT NULL DEFAULT 0      COMMENT '是否可与优惠券叠加：0-不可 1-可叠加',
    `max_discount`   DECIMAL(10,2) NOT NULL DEFAULT 0.00   COMMENT '叠加时最高优惠金额（0表示不限制）',
    `status`         TINYINT       NOT NULL DEFAULT 1      COMMENT '状态：0-禁用 1-启用',
    `description`    VARCHAR(500)  DEFAULT NULL            COMMENT '活动说明',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_time_range` (`start_time`, `end_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='促销活动表';

-- -----------------------------------------------------------
-- 5. 活动参与记录表 activity_participation
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `activity_participation`;
CREATE TABLE `activity_participation` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '参与记录ID',
    `activity_id` BIGINT   NOT NULL                COMMENT '活动ID',
    `user_id`     BIGINT   NOT NULL                COMMENT '用户ID',
    `order_id`    BIGINT   NOT NULL                COMMENT '关联订单ID',
    `order_no`    VARCHAR(32) NOT NULL             COMMENT '订单编号',
    `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '本单优惠金额',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '参与时间',
    PRIMARY KEY (`id`),
    KEY `idx_activity_user` (`activity_id`, `user_id`),
    KEY `idx_order_id` (`order_id`),
    UNIQUE KEY `uk_activity_user_order` (`activity_id`, `user_id`, `order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='活动参与记录表';

-- -----------------------------------------------------------
-- 6. 会员积分记录表 point_record
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `point_record`;
CREATE TABLE `point_record` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '积分记录ID',
    `user_id`     BIGINT        NOT NULL                COMMENT '用户ID',
    `points`      INT           NOT NULL                COMMENT '积分变动（正数=获得，负数=消费）',
    `type`        TINYINT       NOT NULL DEFAULT 1      COMMENT '类型：1-订单完成 2-积分兑换 3-活动赠送 4-管理员调整',
    `remark`      VARCHAR(200)  DEFAULT NULL            COMMENT '备注说明',
    `order_id`    BIGINT        DEFAULT NULL            COMMENT '关联订单ID',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会员积分记录表';

-- -----------------------------------------------------------
-- 7. 插入示例促销活动数据
-- -----------------------------------------------------------
INSERT INTO `promotion_activity` (`activity_name`, `activity_type`, `rule_config`, `start_time`, `end_time`, `user_limit`, `stackable`, `max_discount`, `status`, `description`) VALUES
('暑期租车满500减80', 1, '{"minAmount":500,"discountAmount":80}', '2026-07-01 00:00:00', '2026-09-01 23:59:59', 2, 1, 80.00, 1, '暑期特惠，租车满500元立减80元，可与优惠券叠加'),
('周末嗨玩85折', 2, '{"minAmount":200,"discountRate":0.85}', '2026-08-01 00:00:00', '2026-12-31 23:59:59', 1, 0, 0.00, 1, '周末租车享85折优惠，不可与优惠券叠加'),
('新用户首单立减50', 3, '{"minAmount":100,"discountAmount":50}', '2026-08-01 00:00:00', '2026-12-31 23:59:59', 1, 0, 0.00, 1, '新用户首次租车立减50元');
