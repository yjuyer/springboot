/*
 * ============================================================
 * 新能源汽车租赁管理平台 - 数据库初始化脚本
 * New Energy Vehicle Rental Management Platform
 * ============================================================
 * 版本: 1.0
 * 创建时间: 2026-05-28
 * 说明: 包含全部表结构、索引、外键约束及初始数据
 * ============================================================
 */

-- -----------------------------------------------------------
-- 1. 删除旧数据库并重建
-- -----------------------------------------------------------
DROP DATABASE IF EXISTS `nev_rental`;
CREATE DATABASE `nev_rental`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `nev_rental`;

-- ===================== 系统管理模块 ========================

-- -----------------------------------------------------------
-- 2. 用户表 sys_user
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`          VARCHAR(50)   NOT NULL                COMMENT '用户名（登录账号）',
    `password`          VARCHAR(200)  NOT NULL                COMMENT '密码（BCrypt加密）',
    `real_name`         VARCHAR(50)   DEFAULT NULL            COMMENT '真实姓名',
    `phone`             VARCHAR(20)   DEFAULT NULL            COMMENT '手机号',
    `email`             VARCHAR(100)  DEFAULT NULL            COMMENT '邮箱',
    `avatar`            VARCHAR(500)  DEFAULT NULL            COMMENT '头像URL',
    `id_card`           VARCHAR(18)   DEFAULT NULL            COMMENT '身份证号',
    `id_card_verified`  TINYINT       NOT NULL DEFAULT 0      COMMENT '实名认证状态：0-未认证 1-已认证',
    `driver_license`    VARCHAR(500)  DEFAULT NULL            COMMENT '驾驶证图片路径',
    `license_verified`  TINYINT       NOT NULL DEFAULT 0      COMMENT '驾驶证审核状态：0-未审核 1-审核中 2-已通过 3-未通过',
    `credit_score`      INT           NOT NULL DEFAULT 100    COMMENT '信用积分',
    `member_level`      TINYINT       NOT NULL DEFAULT 0      COMMENT '会员等级：0-白银 1-黄金 2-白金 3-钻石 4-黑金',
    `total_spent`       DECIMAL(12,2) NOT NULL DEFAULT 0.00   COMMENT '累计消费金额（元）',
    `member_points`     INT           NOT NULL DEFAULT 0      COMMENT '会员积分（订单金额1元=1积分，押金不算）',
    `free_cancel_count` INT           NOT NULL DEFAULT 0      COMMENT '本月已使用免费取消次数',
    `status`            TINYINT       NOT NULL DEFAULT 1      COMMENT '状态：0-禁用 1-启用',
    `create_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- -----------------------------------------------------------
-- 3. 角色表 sys_role
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name`   VARCHAR(50)  NOT NULL                COMMENT '角色名称',
    `role_code`   VARCHAR(50)  NOT NULL                COMMENT '角色编码',
    `description` VARCHAR(200) DEFAULT NULL            COMMENT '角色描述',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：0-禁用 1-启用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';

-- -----------------------------------------------------------
-- 4. 权限表 sys_permission
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `permission_name` VARCHAR(50)  NOT NULL                COMMENT '权限名称',
    `permission_code` VARCHAR(100) NOT NULL                COMMENT '权限编码',
    `type`            VARCHAR(10)  NOT NULL DEFAULT 'menu' COMMENT '类型：menu-菜单 button-按钮',
    `parent_id`       BIGINT       DEFAULT 0               COMMENT '父权限ID（0为顶级）',
    `path`            VARCHAR(200) DEFAULT NULL            COMMENT '路由路径 / API路径',
    `icon`            VARCHAR(50)  DEFAULT NULL            COMMENT '图标',
    `sort_order`      INT          NOT NULL DEFAULT 0      COMMENT '排序号',
    `status`          TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：0-禁用 1-启用',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='权限表';

-- -----------------------------------------------------------
-- 5. 用户角色关联表 sys_user_role
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id`      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL                COMMENT '用户ID',
    `role_id` BIGINT NOT NULL                COMMENT '角色ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`),
    CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户角色关联表';

-- -----------------------------------------------------------
-- 6. 角色权限关联表 sys_role_permission
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id`       BIGINT NOT NULL                COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL                COMMENT '权限ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`),
    CONSTRAINT `fk_rp_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_rp_perm` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色权限关联表';

-- ===================== 门店管理模块 ========================

-- -----------------------------------------------------------
-- 7. 门店表 store
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `store`;
CREATE TABLE `store` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '门店ID',
    `store_name`    VARCHAR(100) NOT NULL                COMMENT '门店名称',
    `address`       VARCHAR(200) NOT NULL                COMMENT '详细地址',
    `city`          VARCHAR(50)  NOT NULL                COMMENT '所在城市',
    `province`      VARCHAR(50)  NOT NULL                COMMENT '所在省份',
    `longitude`     DECIMAL(10,7) DEFAULT NULL           COMMENT '经度',
    `latitude`      DECIMAL(10,7) DEFAULT NULL           COMMENT '纬度',
    `phone`         VARCHAR(20)  DEFAULT NULL            COMMENT '联系电话',
    `manager_name`  VARCHAR(50)  DEFAULT NULL            COMMENT '负责人姓名',
    `status`        TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：0-暂停营业 1-正常营业',
    `business_hours` VARCHAR(50) DEFAULT '08:00-22:00'  COMMENT '营业时间',
    `description`   VARCHAR(500) DEFAULT NULL            COMMENT '门店简介',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_city` (`city`),
    KEY `idx_province` (`province`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='门店表';

-- ===================== 车辆管理模块 ========================

-- -----------------------------------------------------------
-- 8. 品牌表 vehicle_brand
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `vehicle_brand`;
CREATE TABLE `vehicle_brand` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '品牌ID',
    `brand_name`  VARCHAR(50)  NOT NULL                COMMENT '品牌名称',
    `logo`        VARCHAR(500) DEFAULT NULL            COMMENT '品牌Logo路径',
    `country`     VARCHAR(50)  DEFAULT NULL            COMMENT '品牌所属国家',
    `description` VARCHAR(500) DEFAULT NULL            COMMENT '品牌描述',
    `sort_order`  INT          NOT NULL DEFAULT 0      COMMENT '排序号',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_brand_name` (`brand_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='车辆品牌表';

-- -----------------------------------------------------------
-- 9. 车辆表 vehicle
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `vehicle`;
CREATE TABLE `vehicle` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '车辆ID',
    `brand_id`         BIGINT        NOT NULL                COMMENT '品牌ID',
    `model`            VARCHAR(100)  NOT NULL                COMMENT '车型名称',
    `vehicle_type`     VARCHAR(20)   NOT NULL                COMMENT '车辆类型：轿车/SUV/MPV/跑车',
    `license_plate`    VARCHAR(20)   NOT NULL                COMMENT '车牌号',
    `color`            VARCHAR(20)   DEFAULT NULL            COMMENT '车身颜色',
    `seat_count`       TINYINT       NOT NULL DEFAULT 5      COMMENT '座位数',
    `daily_price`      DECIMAL(10,2) NOT NULL                COMMENT '日租金（元）',
    `deposit`          DECIMAL(10,2) NOT NULL                COMMENT '押金（元）',
    `battery_capacity` DECIMAL(6,2)  DEFAULT NULL            COMMENT '电池容量（kWh）',
    `range_km`         INT           DEFAULT NULL            COMMENT '续航里程（km）',
    `current_battery`  TINYINT       NOT NULL DEFAULT 100    COMMENT '当前电量百分比（0-100）',
    `charging_status`  TINYINT       NOT NULL DEFAULT 0      COMMENT '充电状态：0-未充电 1-充电中 2-已充满',
    `vehicle_status`   TINYINT       NOT NULL DEFAULT 0      COMMENT '车辆状态：0-空闲 1-已预约 2-租赁中 3-维修中 4-充电中 5-调度中',
    `mileage`          DECIMAL(10,2) NOT NULL DEFAULT 0.00   COMMENT '总行驶里程（km）',
    `image`            VARCHAR(500)  DEFAULT NULL            COMMENT '主图URL',
    `images`           JSON          DEFAULT NULL            COMMENT '多图URL列表（JSON数组）',
    `store_id`         BIGINT        NOT NULL                COMMENT '所属门店ID',
    `description`      VARCHAR(500)  DEFAULT NULL            COMMENT '车辆描述',
    `create_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_license_plate` (`license_plate`),
    KEY `idx_brand_id` (`brand_id`),
    KEY `idx_store_id` (`store_id`),
    KEY `idx_vehicle_status` (`vehicle_status`),
    KEY `idx_vehicle_type` (`vehicle_type`),
    CONSTRAINT `fk_vehicle_brand` FOREIGN KEY (`brand_id`) REFERENCES `vehicle_brand` (`id`),
    CONSTRAINT `fk_vehicle_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='车辆表';

-- ===================== 订单支付模块 ========================

-- -----------------------------------------------------------
-- 10. 租赁订单表 rental_order
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `rental_order`;
CREATE TABLE `rental_order` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no`          VARCHAR(50)   NOT NULL                COMMENT '订单编号',
    `user_id`           BIGINT        NOT NULL                COMMENT '用户ID',
    `vehicle_id`        BIGINT        NOT NULL                COMMENT '车辆ID',
    `pickup_store_id`   BIGINT        NOT NULL                COMMENT '取车门店ID',
    `return_store_id`   BIGINT        NOT NULL                COMMENT '还车门店ID',
    `pickup_time`       DATETIME      NOT NULL                COMMENT '预约取车时间',
    `return_time`       DATETIME      NOT NULL                COMMENT '预约还车时间',
    `actual_return_time` DATETIME     DEFAULT NULL            COMMENT '实际还车时间',
    `daily_price`       DECIMAL(10,2) NOT NULL                COMMENT '日租单价（元）',
    `rental_days`       INT           NOT NULL                COMMENT '租赁天数',
    `total_amount`      DECIMAL(10,2) NOT NULL                COMMENT '订单总金额（元）',
    `deposit_amount`    DECIMAL(10,2) NOT NULL                COMMENT '押金金额（元）',
    `paid_amount`       DECIMAL(10,2) NOT NULL DEFAULT 0.00  COMMENT '实际支付金额（元）',
    `order_status`      TINYINT       NOT NULL DEFAULT 0      COMMENT '订单状态：0-待支付 1-已支付 2-待取车 3-租赁中 4-待还车 5-已完成 6-已取消 7-退款中 8-已退款',
    `pay_type`          VARCHAR(20)   DEFAULT NULL            COMMENT '支付方式：alipay/wechat/bank',
    `pay_time`          DATETIME      DEFAULT NULL            COMMENT '支付时间',
    `cancel_reason`     VARCHAR(200)  DEFAULT NULL            COMMENT '取消原因',
    `pickup_battery`    TINYINT       DEFAULT NULL            COMMENT '取车时电量百分比',
    `return_battery`    TINYINT       DEFAULT NULL            COMMENT '还车时电量百分比',
    `pickup_mileage`    DECIMAL(10,2) DEFAULT NULL            COMMENT '取车时总里程（km）',
    `return_mileage`    DECIMAL(10,2) DEFAULT NULL            COMMENT '还车时总里程（km）',
    `remark`            VARCHAR(500)  DEFAULT NULL            COMMENT '备注',
    `coupon_id`         BIGINT        DEFAULT NULL            COMMENT '使用的优惠券ID',
    `discount_amount`   DECIMAL(10,2) NOT NULL DEFAULT 0.00   COMMENT '优惠金额（元）',
    `create_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_vehicle_id` (`vehicle_id`),
    KEY `idx_order_status` (`order_status`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_order_user`    FOREIGN KEY (`user_id`)    REFERENCES `sys_user` (`id`),
    CONSTRAINT `fk_order_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicle` (`id`),
    CONSTRAINT `fk_order_pickup`  FOREIGN KEY (`pickup_store_id`) REFERENCES `store` (`id`),
    CONSTRAINT `fk_order_return`  FOREIGN KEY (`return_store_id`) REFERENCES `store` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='租赁订单表';

-- -----------------------------------------------------------
-- 11. 支付记录表 payment_record
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `payment_record`;
CREATE TABLE `payment_record` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `order_id`       BIGINT        NOT NULL                COMMENT '订单ID',
    `order_no`       VARCHAR(50)   NOT NULL                COMMENT '订单编号',
    `user_id`        BIGINT        NOT NULL                COMMENT '用户ID',
    `amount`         DECIMAL(10,2) NOT NULL                COMMENT '支付金额（元）',
    `pay_type`       VARCHAR(20)   NOT NULL                COMMENT '支付方式：alipay/wechat/bank',
    `pay_status`     TINYINT       NOT NULL DEFAULT 0      COMMENT '支付状态：0-待支付 1-支付成功 2-支付失败 3-已退款',
    `transaction_no` VARCHAR(100)  DEFAULT NULL            COMMENT '第三方交易流水号',
    `pay_time`       DATETIME      DEFAULT NULL            COMMENT '支付完成时间',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_order_no` (`order_no`),
    CONSTRAINT `fk_pay_order` FOREIGN KEY (`order_id`) REFERENCES `rental_order` (`id`),
    CONSTRAINT `fk_pay_user`  FOREIGN KEY (`user_id`)  REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支付记录表';

-- -----------------------------------------------------------
-- 12. 车辆评价表 vehicle_review
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `vehicle_review`;
CREATE TABLE `vehicle_review` (
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
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_review_order` FOREIGN KEY (`order_id`) REFERENCES `rental_order` (`id`),
    CONSTRAINT `fk_review_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
    CONSTRAINT `fk_review_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicle` (`id`),
    CONSTRAINT `fk_review_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='车辆评价表';

-- ===================== 信用管理模块 ========================

-- -----------------------------------------------------------
-- 12. 信用记录表 credit_record
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `credit_record`;
CREATE TABLE `credit_record` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id`       BIGINT       NOT NULL                COMMENT '用户ID',
    `change_type`   VARCHAR(10)  NOT NULL                COMMENT '变更类型：加分/扣分',
    `change_amount` INT          NOT NULL                COMMENT '变更分值',
    `before_score`  INT          NOT NULL                COMMENT '变更前积分',
    `after_score`   INT          NOT NULL                COMMENT '变更后积分',
    `reason`        VARCHAR(200) NOT NULL                COMMENT '变更原因',
    `order_id`      BIGINT       DEFAULT NULL            COMMENT '关联订单ID',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`       TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_order_id` (`order_id`),
    CONSTRAINT `fk_credit_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='信用记录表';

-- -----------------------------------------------------------
-- 13. 信用规则配置表 credit_rule
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `credit_rule`;
CREATE TABLE `credit_rule` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `rule_name`     VARCHAR(50)  NOT NULL                COMMENT '规则名称',
    `rule_code`     VARCHAR(50)  NOT NULL                COMMENT '规则编码',
    `change_type`   VARCHAR(10)  NOT NULL                COMMENT '变更类型：加分/扣分',
    `change_amount` INT          NOT NULL                COMMENT '变更分值',
    `description`   VARCHAR(200) DEFAULT NULL            COMMENT '规则描述',
    `status`        TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：0-禁用 1-启用',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rule_code` (`rule_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='信用规则配置表';

-- ===================== 运维管理模块 ========================

-- -----------------------------------------------------------
-- 14. 车辆调度记录表 vehicle_dispatch
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `vehicle_dispatch`;
CREATE TABLE `vehicle_dispatch` (
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
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_dispatch_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicle` (`id`),
    CONSTRAINT `fk_dispatch_from_store` FOREIGN KEY (`from_store_id`) REFERENCES `store` (`id`),
    CONSTRAINT `fk_dispatch_to_store` FOREIGN KEY (`to_store_id`) REFERENCES `store` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='车辆调度记录表';

-- -----------------------------------------------------------
-- 15. 维修记录表 repair_record
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `repair_record`;
CREATE TABLE `repair_record` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `vehicle_id`  BIGINT        NOT NULL                COMMENT '车辆ID',
    `repair_type` VARCHAR(50)   NOT NULL                COMMENT '维修类型',
    `description` VARCHAR(500)  NOT NULL                COMMENT '故障描述',
    `start_time`  DATETIME      DEFAULT NULL            COMMENT '维修开始时间',
    `end_time`    DATETIME      DEFAULT NULL            COMMENT '维修完成时间',
    `cost`        DECIMAL(10,2) NOT NULL DEFAULT 0.00   COMMENT '维修费用（元）',
    `status`      TINYINT       NOT NULL DEFAULT 0      COMMENT '状态：0-待维修 1-维修中 2-已完成',
    `operator_id` BIGINT        DEFAULT NULL            COMMENT '操作人ID',
    `remark`      VARCHAR(500)  DEFAULT NULL            COMMENT '备注',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_vehicle_id` (`vehicle_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_repair_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicle` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='维修记录表';

-- -----------------------------------------------------------
-- 15. 充电记录表 charging_record
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `charging_record`;
CREATE TABLE `charging_record` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `vehicle_id`    BIGINT        NOT NULL                COMMENT '车辆ID',
    `station_name`  VARCHAR(100)  DEFAULT NULL            COMMENT '充电站名称',
    `start_battery` TINYINT       NOT NULL                COMMENT '起始电量百分比',
    `end_battery`   TINYINT       DEFAULT NULL            COMMENT '结束电量百分比',
    `start_time`    DATETIME      NOT NULL                COMMENT '充电开始时间',
    `end_time`      DATETIME      DEFAULT NULL            COMMENT '充电结束时间',
    `cost`          DECIMAL(10,2) NOT NULL DEFAULT 0.00   COMMENT '充电费用（元）',
    `status`        TINYINT       NOT NULL DEFAULT 0      COMMENT '状态：0-充电中 1-已完成',
    `operator_id`   BIGINT        DEFAULT NULL            COMMENT '操作人ID',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_vehicle_id` (`vehicle_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_charge_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicle` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='充电记录表';

-- ===================== 通知公告模块 ========================

-- -----------------------------------------------------------
-- 16. 公告表 notice
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `title`        VARCHAR(200)  NOT NULL                COMMENT '公告标题',
    `content`      TEXT          NOT NULL                COMMENT '公告内容',
    `type`         VARCHAR(20)   NOT NULL DEFAULT 'notice' COMMENT '类型：notice-通知 announcement-公告 activity-活动',
    `publisher_id` BIGINT        NOT NULL                COMMENT '发布人ID',
    `status`       TINYINT       NOT NULL DEFAULT 1      COMMENT '状态：0-草稿 1-已发布 2-已撤回',
    `is_top`       TINYINT       NOT NULL DEFAULT 0      COMMENT '是否置顶：0-否 1-是',
    `publish_time` DATETIME      DEFAULT NULL            COMMENT '发布时间',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_publish_time` (`publish_time`),
    CONSTRAINT `fk_notice_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告表';

-- -----------------------------------------------------------
-- 17. 系统通知表 notification（WebSocket推送）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id`    BIGINT       NOT NULL                COMMENT '接收用户ID',
    `title`      VARCHAR(200) NOT NULL                COMMENT '通知标题',
    `content`    VARCHAR(500) NOT NULL                COMMENT '通知内容',
    `type`       VARCHAR(20)  NOT NULL DEFAULT 'system' COMMENT '类型：system-系统 order-订单 payment-支付 credit-信用',
    `is_read`    TINYINT      NOT NULL DEFAULT 0      COMMENT '是否已读：0-未读 1-已读',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`    TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_notif_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统通知表';

-- ===================== 押金管理模块 ========================

-- -----------------------------------------------------------
-- 18. 押金记录表 deposit_record
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `deposit_record`;
CREATE TABLE `deposit_record` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `order_id`    BIGINT        NOT NULL                COMMENT '订单ID',
    `order_no`    VARCHAR(50)   NOT NULL                COMMENT '订单编号',
    `user_id`     BIGINT        NOT NULL                COMMENT '用户ID',
    `vehicle_id`  BIGINT        NOT NULL                COMMENT '车辆ID',
    `amount`      DECIMAL(10,2) NOT NULL                COMMENT '押金金额（元）',
    `status`      TINYINT       NOT NULL DEFAULT 0      COMMENT '押金状态：0-待支付 1-已冻结 2-已退款',
    `freeze_time` DATETIME      DEFAULT NULL            COMMENT '冻结时间',
    `refund_time` DATETIME      DEFAULT NULL            COMMENT '退款时间',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_deposit_order` FOREIGN KEY (`order_id`) REFERENCES `rental_order` (`id`),
    CONSTRAINT `fk_deposit_user`  FOREIGN KEY (`user_id`)  REFERENCES `sys_user` (`id`),
    CONSTRAINT `fk_deposit_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicle` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='押金记录表';

-- -----------------------------------------------------------
-- 19. 押金退款表 deposit_refund
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `deposit_refund`;
CREATE TABLE `deposit_refund` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '退款ID',
    `deposit_id`    BIGINT        NOT NULL                COMMENT '押金记录ID',
    `order_id`      BIGINT        NOT NULL                COMMENT '订单ID',
    `order_no`      VARCHAR(50)   NOT NULL                COMMENT '订单编号',
    `user_id`       BIGINT        NOT NULL                COMMENT '用户ID',
    `amount`        DECIMAL(10,2) NOT NULL                COMMENT '退款金额（元）',
    `refund_type`   TINYINT       NOT NULL DEFAULT 1      COMMENT '退款方式：1-原路退回 2-线下转账',
    `refund_reason` VARCHAR(200)  DEFAULT NULL            COMMENT '退款原因',
    `operator_id`   BIGINT        DEFAULT NULL            COMMENT '操作人ID',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_deposit_id` (`deposit_id`),
    CONSTRAINT `fk_refund_deposit` FOREIGN KEY (`deposit_id`) REFERENCES `deposit_record` (`id`),
    CONSTRAINT `fk_refund_order`   FOREIGN KEY (`order_id`)   REFERENCES `rental_order` (`id`),
    CONSTRAINT `fk_refund_user`    FOREIGN KEY (`user_id`)    REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='押金退款表';

-- ============================================================
--                     初始数据
-- ============================================================

-- -----------------------------------------------------------
-- 角色数据
-- -----------------------------------------------------------
INSERT INTO `sys_role` (`role_name`, `role_code`, `description`, `status`) VALUES
('管理员',   'ADMIN',    '系统管理员，拥有全部权限',   1),
('运营人员', 'OPERATOR', '门店运营人员，管理车辆与订单', 1),
('普通用户', 'USER',     '普通注册用户，可租赁车辆',   1);

-- -----------------------------------------------------------
-- 权限数据（10个菜单权限）
-- -----------------------------------------------------------
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `type`, `parent_id`, `path`, `icon`, `sort_order`) VALUES
('系统管理',   'system',          'menu', 0, '/system',          'setting',  1),
('用户管理',   'system:user',     'menu', 1, '/system/user',     'user',     2),
('角色管理',   'system:role',     'menu', 1, '/system/role',     'peoples',  3),
('权限管理',   'system:perm',     'menu', 1, '/system/perm',     'lock',     4),
('门店管理',   'store',           'menu', 0, '/store',           'shop',     5),
('车辆管理',   'vehicle',         'menu', 0, '/vehicle',         'car',      6),
('订单管理',   'order',           'menu', 0, '/order',           'order',    7),
('财务管理',   'finance',         'menu', 0, '/finance',         'money',    8),
('公告管理',   'notice',          'menu', 0, '/notice',          'message',  9),
('数据统计',   'statistics',      'menu', 0, '/statistics',      'chart',   10);

-- -----------------------------------------------------------
-- 管理员用户（密码: admin123，BCrypt加密）
-- BCrypt(10 rounds) for "admin123":
-- $2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2
-- 会员等级：4-黑金会员（最高级），累计消费：15000元
-- -----------------------------------------------------------
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `email`, `id_card_verified`, `license_verified`, `credit_score`, `status`, `member_level`, `total_spent`, `member_points`, `free_cancel_count`) VALUES
('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', '13800000000', 'admin@nev-rental.com', 1, 2, 100, 1, 4, 15000.00, 15000, 0);

-- -----------------------------------------------------------
-- 管理员绑定ADMIN角色
-- -----------------------------------------------------------
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- -----------------------------------------------------------
-- 管理员角色拥有全部10个权限
-- -----------------------------------------------------------
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10);

-- 运营人员拥有车辆、门店、订单、公告、统计权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(2,5),(2,6),(2,7),(2,9),(2,10);

-- -----------------------------------------------------------
-- 品牌数据
-- -----------------------------------------------------------
INSERT INTO `vehicle_brand` (`brand_name`, `country`, `description`, `sort_order`) VALUES
('比亚迪', '中国', '全球新能源汽车销量领先品牌', 1),
('特斯拉', '美国', '全球知名电动汽车品牌',      2),
('蔚来',   '中国', '高端智能电动汽车品牌',      3),
('小鹏',   '中国', '智能电动汽车品牌，注重科技感', 4),
('理想',   '中国', '增程式电动汽车品牌',        5),
('问界',   '中国', '华为与赛力斯联合打造的高端智能汽车品牌', 6),
('极氪',   '中国', '吉利旗下高端纯电品牌',      7),
('零跑',   '中国', '全域自研智能电动汽车品牌',  8);

-- -----------------------------------------------------------
-- 门店数据（5个城市）
-- -----------------------------------------------------------
INSERT INTO `store` (`store_name`, `address`, `city`, `province`, `longitude`, `latitude`, `phone`, `manager_name`, `business_hours`, `description`) VALUES
('北京朝阳旗舰店',   '北京市朝阳区建国路88号',     '北京', '北京市', 116.4683000, 39.9149000, '010-88880001', '张伟', '08:00-22:00', '旗舰门店，车型齐全'),
('上海浦东中心店',   '上海市浦东新区陆家嘴环路1000号','上海', '上海市', 121.5015000, 31.2372000, '021-68880002', '李娜', '08:00-22:00', '浦东核心商圈'),
('深圳南山科技园店', '深圳市南山区科技园南区深南大道9966号','深圳','广东省', 113.9467000, 22.5362000, '0755-86660003', '王强', '08:00-23:00', '科技园附近，充电设施完善'),
('广州天河体育中心店','广州市天河区体育西路191号',  '广州', '广东省', 113.3246000, 23.1365000, '020-38880004', '陈芳', '07:30-22:00', '市中心交通枢纽'),
('杭州西湖文化广场店','杭州市下城区武林广场21号',   '杭州', '浙江省', 120.1697000, 30.2741000, '0571-87770005', '赵明', '08:00-22:00', '西湖景区附近，游客首选');

-- -----------------------------------------------------------
-- 车辆数据（32辆，覆盖8个品牌）
-- -----------------------------------------------------------
INSERT INTO `vehicle` (`brand_id`, `model`, `vehicle_type`, `license_plate`, `color`, `seat_count`, `daily_price`, `deposit`, `battery_capacity`, `range_km`, `current_battery`, `charging_status`, `vehicle_status`, `mileage`, `store_id`, `description`) VALUES
-- 比亚迪（6款）
(1, '汉EV',       '轿车', '京A·NE0001', '黑色',   5, 299.00, 3000.00, 85.44,  605, 95, 0, 0, 15230.50, 1, '比亚迪旗舰轿车，刀片电池，续航优秀'),
(1, '唐EV',       'SUV',  '京A·NE0002', '白色',   7, 359.00, 3500.00, 108.80, 600, 88, 0, 0, 8760.20,  1, '七座纯电SUV，空间宽敞'),
(1, '海豹',       '轿车', '沪A·NE0003', '蓝色',   5, 269.00, 2500.00, 82.56,  700, 100, 2, 0, 3200.00,  2, '运动轿跑，操控出色'),
(1, '秦PLUS EV',  '轿车', '沪A·NE0013', '灰色',   5, 199.00, 2000.00, 57.00,  500, 90, 0, 0, 18500.00, 2, '紧凑型轿车，性价比之王'),
(1, '宋PLUS EV',  'SUV',  '粤B·NE0014', '白色',   5, 259.00, 2500.00, 71.80,  505, 85, 0, 0, 12300.00, 3, '紧凑型SUV，家用首选'),
(1, '元PLUS',     'SUV',  '粤A·NE0015', '红色',   5, 189.00, 2000.00, 60.48,  430, 92, 0, 0, 9800.00,  4, '小型SUV，城市通勤利器'),
-- 特斯拉（4款）
(2, 'Model 3',    '轿车', '沪A·NE0004', '灰色',   5, 329.00, 3000.00, 60.00,  556, 72, 0, 0, 22150.80, 2, '特斯拉经典车型，智能驾驶辅助'),
(2, 'Model Y',    'SUV',  '粤B·NE0005', '白色',   5, 399.00, 4000.00, 78.40,  640, 90, 0, 0, 11500.00, 3, '特斯拉热销SUV，家用首选'),
(2, 'Model S',    '轿车', '京A·NE0016', '黑色',   5, 599.00, 6000.00, 100.00, 715, 88, 0, 0, 5600.00,  1, '特斯拉旗舰轿车，极致性能'),
(2, 'Model X',    'SUV',  '沪A·NE0017', '蓝色',   5, 699.00, 7000.00, 100.00, 580, 75, 0, 0, 3200.00,  2, '特斯拉旗舰SUV，鹰翼门设计'),
-- 蔚来（4款）
(3, 'ES6',        'SUV',  '粤B·NE0006', '深空蓝', 5, 459.00, 5000.00, 100.00, 610, 65, 0, 2, 18900.30, 3, '蔚来智能电动SUV，支持换电'),
(3, 'ET5',        '轿车', '粤A·NE0007', '极光绿', 5, 389.00, 4000.00, 100.00, 710, 100, 2, 0, 5600.00,  4, '蔚来中型轿车，颜值担当'),
(3, 'ES8',        'SUV',  '京A·NE0018', '星空蓝', 6, 559.00, 5500.00, 150.00, 580, 82, 0, 0, 7800.00,  1, '蔚来旗舰SUV，六座豪华'),
(3, 'ET7',        '轿车', '沪A·NE0019', '云白',   5, 499.00, 5000.00, 150.00, 700, 90, 0, 0, 4500.00,  2, '蔚来旗舰轿车，超长续航'),
-- 小鹏（4款）
(4, 'P7',         '轿车', '粤A·NE0008', '红色',   5, 289.00, 2500.00, 80.87,  670, 82, 0, 0, 14320.60, 4, '小鹏智能轿跑，NGP智能导航辅助'),
(4, 'G9',         'SUV',  '浙A·NE0009', '黑色',   5, 429.00, 4500.00, 98.00,  702, 45, 1, 4, 7800.00,  5, '小鹏旗舰SUV，800V快充平台'),
(4, 'P5',         '轿车', '粤B·NE0020', '白色',   5, 229.00, 2500.00, 66.00,  550, 88, 0, 0, 15600.00, 3, '小鹏家用轿车，智能座舱'),
(4, 'G6',         'SUV',  '浙A·NE0021', '银色',   5, 329.00, 3500.00, 87.50,  580, 95, 0, 0, 6200.00,  5, '小鹏中型SUV，性价比之选'),
-- 理想（4款）
(5, 'L7',         'SUV',  '浙A·NE0010', '银色',   5, 379.00, 3500.00, 42.80,  1315, 92, 0, 0, 9500.00,  5, '理想增程式SUV，无里程焦虑'),
(5, 'L9',         'SUV',  '浙A·NE0011', '黑色',   6, 499.00, 5000.00, 44.50,  1315, 78, 0, 1, 6200.00,  5, '理想全尺寸旗舰SUV，奶爸车'),
(5, 'MEGA',       'MPV',  '浙A·NE0012', '白色',   7, 559.00, 6000.00, 102.70, 710, 55, 0, 0, 2100.00,  5, '理想纯电MPV，超大空间'),
(5, 'L6',         'SUV',  '京A·NE0022', '灰色',   5, 329.00, 3000.00, 36.80,  1200, 85, 0, 0, 8900.00,  1, '理想中型SUV，家庭用车'),
-- 问界（4款）
(6, 'M5 EV',      'SUV',  '京A·NE0023', '黑色',   5, 359.00, 3500.00, 80.00,  620, 90, 0, 0, 5600.00,  1, '问界纯电SUV，华为智能座舱'),
(6, 'M7',         'SUV',  '沪A·NE0024', '白色',   6, 429.00, 4000.00, 40.00,  1200, 88, 0, 0, 7800.00,  2, '问界增程式SUV，鸿蒙系统'),
(6, 'M9',         'SUV',  '粤B·NE0025', '深蓝',   6, 599.00, 6000.00, 52.00,  1400, 75, 0, 0, 3200.00,  3, '问界旗舰SUV，华为全栈技术'),
(6, 'M5 增程',    'SUV',  '粤A·NE0026', '银色',   5, 329.00, 3000.00, 40.00,  1100, 92, 0, 0, 12500.00, 4, '问界增程版，智能驾驶'),
-- 极氪（4款）
(7, '001',        '轿车', '京A·NE0027', '黑色',   5, 399.00, 4000.00, 100.00, 741, 85, 0, 0, 6800.00,  1, '极氪旗舰轿车，猎装设计'),
(7, '007',        '轿车', '沪A·NE0028', '白色',   5, 329.00, 3500.00, 75.00,  688, 90, 0, 0, 4500.00,  2, '极氪中型轿车，800V快充'),
(7, '009',        'MPV',  '粤B·NE0029', '灰色',   6, 599.00, 6000.00, 140.00, 822, 78, 0, 0, 2100.00,  3, '极氪豪华MPV，商务首选'),
(7, 'X',          'SUV',  '浙A·NE0030', '蓝色',   5, 289.00, 3000.00, 66.00,  560, 95, 0, 0, 9200.00,  5, '极氪紧凑SUV，个性之选'),
-- 零跑（4款）
(8, 'C11',        'SUV',  '京A·NE0031', '白色',   5, 259.00, 2500.00, 90.00,  610, 88, 0, 0, 11200.00, 1, '零跑中型SUV，全域自研'),
(8, 'C01',        '轿车', '沪A·NE0032', '黑色',   5, 229.00, 2500.00, 90.00,  717, 92, 0, 0, 8500.00,  2, '零跑中大型轿车，CTC技术'),
(8, 'C10',        'SUV',  '粤B·NE0033', '银色',   5, 199.00, 2000.00, 69.90,  530, 85, 0, 0, 5600.00,  3, '零跑紧凑SUV，家庭实用'),
(8, 'S01',        '轿车', '粤A·NE0034', '红色',   5, 159.00, 1500.00, 48.00,  451, 90, 0, 0, 15800.00, 4, '零跑小型轿跑，入门首选');

-- -----------------------------------------------------------
-- 信用规则基础配置
-- -----------------------------------------------------------
INSERT INTO `credit_rule` (`rule_name`, `rule_code`, `change_type`, `change_amount`, `description`) VALUES
('完成订单加分',     'ORDER_COMPLETE',    '加分', 5,   '正常完成租赁订单加5分'),
('超时还车扣分',     'LATE_RETURN',       '扣分', 10,  '超过预约还车时间未还车扣10分'),
('违章扣分',         'TRAFFIC_VIOLATION', '扣分', 20,  '租赁期间发生交通违章扣20分'),
('车辆损坏扣分',     'VEHICLE_DAMAGE',    '扣分', 30,  '租赁期间造成车辆损坏扣30分'),
('取消订单扣分',     'ORDER_CANCEL',      '扣分', 2,   '取消已支付订单扣2分'),
('实名认证加分',     'ID_VERIFY',         '加分', 10,  '完成实名认证加10分'),
('驾驶证认证加分',   'LICENSE_VERIFY',    '加分', 10,  '完成驾驶证认证加10分'),
('恶意逾期扣分',     'MALICIOUS_OVERDUE', '扣分', 50,  '恶意逾期不还车扣50分'),
('好评加分',         'GOOD_REVIEW',       '加分', 2,   '给予5星好评加2分'),
('推荐注册加分',     'REFERRAL',          '加分', 5,   '成功推荐新用户注册加5分');

-- -----------------------------------------------------------
-- 公告示例数据
-- -----------------------------------------------------------
INSERT INTO `notice` (`title`, `content`, `type`, `publisher_id`, `status`, `is_top`, `publish_time`) VALUES
('平台上线公告', '新能源汽车租赁管理平台正式上线！支持比亚迪、特斯拉、蔚来、小鹏、理想、问界、极氪、零跑八大品牌新能源车型，涵盖轿车、SUV、MPV等多种车型，欢迎体验！', 'announcement', 1, 1, 1, NOW()),
('五一假期优惠活动', '五一假期期间，全场车辆日租价8折优惠！新用户首单再减50元，名额有限，先到先得！', 'activity', 1, 1, 0, NOW()),
('关于信用积分规则说明', '为保障用户权益和车辆安全，平台推出信用积分制度。正常完成订单可获得积分奖励，违规行为将扣除相应积分。信用分低于60分将限制租车功能。', 'notice', 1, 1, 0, NOW());

-- -----------------------------------------------------------
-- 完成提示
-- -----------------------------------------------------------
SELECT '========================================' AS '';
SELECT ' 新能源汽车租赁管理平台数据库初始化完成' AS '';
SELECT '========================================' AS '';
SELECT CONCAT(' 数据库: ', DATABASE()) AS '';
SELECT CONCAT(' 表数量: ', COUNT(*)) AS '总表数'
FROM information_schema.tables
WHERE table_schema = DATABASE();
