-- 给用户表添加驾驶证最后更改时间字段
USE `nev_rental`;

ALTER TABLE `sys_user`
ADD COLUMN `license_update_time` DATETIME DEFAULT NULL COMMENT '驾驶证最后更改时间' AFTER `license_verified`;
