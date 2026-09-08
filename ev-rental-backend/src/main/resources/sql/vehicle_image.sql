-- =====================================================
-- 新能源汽车租赁平台 - 车辆图片管理模块 SQL
-- 执行前请确保已选择正确的数据库: nev_rental
-- =====================================================

-- 1. 给 vehicle 表添加 cover_image 字段（如果已存在则跳过）
ALTER TABLE vehicle ADD COLUMN IF NOT EXISTS cover_image VARCHAR(500) COMMENT '车辆封面图片URL';

-- 2. 创建 vehicle_image 表
DROP TABLE IF EXISTS vehicle_image;
CREATE TABLE vehicle_image (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    vehicle_id BIGINT NOT NULL COMMENT '车辆ID',
    image_url VARCHAR(500) NOT NULL COMMENT '图片URL',
    sort_num INT DEFAULT 1 COMMENT '排序号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_vehicle_id (vehicle_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆图片表';

-- 3. 初始化测试数据（示例）
-- 假设 vehicle 表中已有车辆数据，为其添加图片
-- 请根据实际车辆ID调整
-- INSERT INTO vehicle_image (vehicle_id, image_url, sort_num) VALUES
-- (1, '/upload/xxx.jpg', 1),
-- (1, '/upload/yyy.jpg', 2),
-- (2, '/upload/zzz.jpg', 1);