-- ===================== 优惠券30天有效期 ========================
-- 给 user_coupon 表添加 expire_time 字段
-- 用户领取优惠券后30天过期

ALTER TABLE `user_coupon`
ADD COLUMN `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间（领取后30天）' AFTER `use_time`;

-- 更新已有记录：已领取但未过期的优惠券设置30天有效期
UPDATE `user_coupon`
SET `expire_time` = DATE_ADD(`get_time`, INTERVAL 30 DAY)
WHERE `status` = 0 AND `expire_time` IS NULL;
