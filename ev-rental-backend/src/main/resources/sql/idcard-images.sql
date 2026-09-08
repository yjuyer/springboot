-- 给用户表添加身份证正反面图片字段
USE `nev_rental`;

ALTER TABLE `sys_user`
ADD COLUMN `id_card_front` VARCHAR(500) DEFAULT NULL COMMENT '身份证正面图片路径' AFTER `id_card`,
ADD COLUMN `id_card_back` VARCHAR(500) DEFAULT NULL COMMENT '身份证背面图片路径' AFTER `id_card_front`;
