-- ===================== 订单表添加优惠券字段 ========================

USE `nev_rental`;

-- 给订单表添加优惠券相关字段
ALTER TABLE `rental_order`
ADD COLUMN `coupon_id` BIGINT DEFAULT NULL COMMENT '使用的优惠券ID' AFTER `remark`,
ADD COLUMN `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额' AFTER `coupon_id`;

-- 添加索引
ALTER TABLE `rental_order` ADD INDEX `idx_coupon_id` (`coupon_id`);
