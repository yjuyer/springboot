-- 给用户表添加会员等级字段
USE `nev_rental`;

ALTER TABLE `sys_user`
ADD COLUMN `member_level` TINYINT NOT NULL DEFAULT 0 COMMENT '会员等级：0-白银 1-黄金 2-白金 3-钻石 4-黑金' AFTER `credit_score`,
ADD COLUMN `total_spent` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计消费金额' AFTER `member_level`,
ADD COLUMN `free_cancel_count` INT NOT NULL DEFAULT 0 COMMENT '本月已使用免费取消次数' AFTER `total_spent`;

-- 创建会员权益表
DROP TABLE IF EXISTS `member_benefit`;
CREATE TABLE `member_benefit` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `level` TINYINT NOT NULL COMMENT '会员等级：0-白银 1-黄金 2-白金 3-钻石 4-黑金',
    `benefit_type` VARCHAR(50) NOT NULL COMMENT '权益类型',
    `benefit_value` VARCHAR(200) DEFAULT NULL COMMENT '权益值',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '权益说明',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会员权益表';

-- 插入会员权益数据
INSERT INTO `member_benefit` (`level`, `benefit_type`, `benefit_value`, `description`) VALUES
-- 白银会员权益
(0, 'COUPON_DISCOUNT', '0.98', '优惠券98折'),
(0, 'BIRTHDAY', '0', '无生日特权'),
(0, 'FREE_CANCEL', '0', '无免费取消次数'),
(0, 'PRIORITY_PICKUP', '0', '无优先取车'),
(0, 'EXCLUSIVE_SERVICE', '0', '无专属客服'),
(0, 'FREE_UPGRADE', '0', '无免费升级'),
(0, 'AIRPORT_TRANSFER', '0', '无机场接送'),
(0, 'EXCLUSIVE_EVENT', '0', '无专属活动'),

-- 黄金会员权益
(1, 'COUPON_DISCOUNT', '0.95', '优惠券95折'),
(1, 'BIRTHDAY', '1', '生日当天8折优惠'),
(1, 'FREE_CANCEL', '1', '每月1次免费取消'),
(1, 'PRIORITY_PICKUP', '0', '无优先取车'),
(1, 'EXCLUSIVE_SERVICE', '0', '无专属客服'),
(1, 'FREE_UPGRADE', '0', '无免费升级'),
(1, 'AIRPORT_TRANSFER', '0', '无机场接送'),
(1, 'EXCLUSIVE_EVENT', '0', '无专属活动'),

-- 白金会员权益
(2, 'COUPON_DISCOUNT', '0.92', '优惠券92折'),
(2, 'BIRTHDAY', '1', '生日当天8折优惠'),
(2, 'FREE_CANCEL', '2', '每月2次免费取消'),
(2, 'PRIORITY_PICKUP', '1', '优先取车'),
(2, 'EXCLUSIVE_SERVICE', '0', '无专属客服'),
(2, 'FREE_UPGRADE', '0', '无免费升级'),
(2, 'AIRPORT_TRANSFER', '0', '无机场接送'),
(2, 'EXCLUSIVE_EVENT', '0', '无专属活动'),

-- 钻石会员权益
(3, 'COUPON_DISCOUNT', '0.88', '优惠券88折'),
(3, 'BIRTHDAY', '1', '生日当天7折优惠'),
(3, 'FREE_CANCEL', '3', '每月3次免费取消'),
(3, 'PRIORITY_PICKUP', '1', '优先取车'),
(3, 'EXCLUSIVE_SERVICE', '1', '专属客服'),
(3, 'FREE_UPGRADE', '1', '免费升级车型'),
(3, 'AIRPORT_TRANSFER', '0', '无机场接送'),
(3, 'EXCLUSIVE_EVENT', '0', '无专属活动'),

-- 黑金会员权益
(4, 'COUPON_DISCOUNT', '0.85', '优惠券85折'),
(4, 'BIRTHDAY', '1', '生日当天6折优惠'),
(4, 'FREE_CANCEL', '5', '每月5次免费取消'),
(4, 'PRIORITY_PICKUP', '1', '优先取车'),
(4, 'EXCLUSIVE_SERVICE', '1', '专属客服'),
(4, 'FREE_UPGRADE', '1', '免费升级车型'),
(4, 'AIRPORT_TRANSFER', '1', '免费机场接送'),
(4, 'EXCLUSIVE_EVENT', '1', '专属活动邀请');
