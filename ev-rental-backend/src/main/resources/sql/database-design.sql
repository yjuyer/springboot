-- ================================================================
--  新能源汽车租赁管理平台 - 简化版数据库
--  MySQL 8.0 / InnoDB / UTF8MB4
-- ================================================================

DROP DATABASE IF EXISTS nev_rental;
CREATE DATABASE nev_rental
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
USE nev_rental;


-- ====================== 1. 用户表 ======================
CREATE TABLE user (
    id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    username        VARCHAR(50)  NOT NULL                  COMMENT '用户名',
    password        VARCHAR(200) NOT NULL                  COMMENT '密码',
    real_name       VARCHAR(50)  DEFAULT NULL              COMMENT '真实姓名',
    phone           VARCHAR(20)  DEFAULT NULL              COMMENT '手机号',
    email           VARCHAR(100) DEFAULT NULL              COMMENT '邮箱',
    avatar          VARCHAR(500) DEFAULT NULL              COMMENT '头像',
    id_card         VARCHAR(18)  DEFAULT NULL              COMMENT '身份证号',
    id_card_status  TINYINT      NOT NULL DEFAULT 0        COMMENT '实名状态: 0未认证 1已认证',
    license_img     VARCHAR(500) DEFAULT NULL              COMMENT '驾驶证图片',
    license_status  TINYINT      NOT NULL DEFAULT 0        COMMENT '驾照状态: 0未上传 1待审核 2通过 3拒绝',
    credit          INT          NOT NULL DEFAULT 100      COMMENT '信用分',
    role            VARCHAR(20)  NOT NULL DEFAULT 'USER'   COMMENT '角色: ADMIN/OPERATOR/USER',
    status          TINYINT      NOT NULL DEFAULT 1        COMMENT '状态: 0禁用 1启用',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_phone (phone)
) COMMENT='用户表';


-- ====================== 2. 门店表 ======================
CREATE TABLE store (
    id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    name            VARCHAR(100) NOT NULL                  COMMENT '门店名称',
    province        VARCHAR(50)  NOT NULL                  COMMENT '省份',
    city            VARCHAR(50)  NOT NULL                  COMMENT '城市',
    address         VARCHAR(300) NOT NULL                  COMMENT '地址',
    longitude       DECIMAL(10,7) DEFAULT NULL             COMMENT '经度',
    latitude        DECIMAL(10,7) DEFAULT NULL             COMMENT '纬度',
    phone           VARCHAR(20)  DEFAULT NULL              COMMENT '电话',
    manager         VARCHAR(50)  DEFAULT NULL              COMMENT '负责人',
    hours           VARCHAR(50)  DEFAULT '08:00-22:00'    COMMENT '营业时间',
    status          TINYINT      NOT NULL DEFAULT 1        COMMENT '状态: 0关闭 1营业',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    KEY idx_city (city)
) COMMENT='门店表';


-- ====================== 3. 车辆表 ======================
CREATE TABLE vehicle (
    id              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
    brand           VARCHAR(50)   NOT NULL                  COMMENT '品牌',
    model           VARCHAR(50)   NOT NULL                  COMMENT '型号',
    type            TINYINT       NOT NULL DEFAULT 1        COMMENT '类型: 1轿车 2SUV 3MPV 4跑车',
    plate           VARCHAR(20)   NOT NULL                  COMMENT '车牌号',
    color           VARCHAR(20)   DEFAULT NULL              COMMENT '颜色',
    seats           INT           NOT NULL DEFAULT 5        COMMENT '座位数',

    -- 新能源特色
    battery_cap     DECIMAL(6,2)  NOT NULL                  COMMENT '电池容量kWh',
    range_km        INT           NOT NULL                  COMMENT '续航km',
    fast_charge     DECIMAL(4,1)  DEFAULT NULL              COMMENT '快充时间h',
    cur_battery     DECIMAL(5,2)  NOT NULL DEFAULT 100      COMMENT '当前电量%',
    charge_status   TINYINT       NOT NULL DEFAULT 0        COMMENT '充电状态: 0未充 1充电中 2充满',

    -- 运营
    price           DECIMAL(10,2) NOT NULL                  COMMENT '日租金',
    deposit         DECIMAL(10,2) NOT NULL DEFAULT 0        COMMENT '押金',
    mileage         DECIMAL(10,2) NOT NULL DEFAULT 0        COMMENT '总里程km',
    status          TINYINT       NOT NULL DEFAULT 0        COMMENT '状态: 0空闲 1已约 2租赁中 3维修 4充电 5调度',
    store_id        BIGINT        NOT NULL                  COMMENT '所属门店',
    description     VARCHAR(500)  DEFAULT NULL              COMMENT '简介',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT       NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uk_plate (plate),
    KEY idx_brand (brand),
    KEY idx_store (store_id),
    KEY idx_status (status)
) COMMENT='车辆表';


-- ====================== 4. 车辆图片表 ======================
CREATE TABLE vehicle_img (
    id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    vehicle_id      BIGINT       NOT NULL                  COMMENT '车辆ID',
    url             VARCHAR(500) NOT NULL                  COMMENT '图片URL',
    is_main         TINYINT      NOT NULL DEFAULT 0        COMMENT '是否主图: 0否 1是',
    sort            INT          NOT NULL DEFAULT 0        COMMENT '排序',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY idx_vehicle (vehicle_id)
) COMMENT='车辆图片表';


-- ====================== 5. 订单表 ======================
CREATE TABLE `order` (
    id              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
    order_no        VARCHAR(30)   NOT NULL                  COMMENT '订单号',
    user_id         BIGINT        NOT NULL                  COMMENT '用户ID',
    vehicle_id      BIGINT        NOT NULL                  COMMENT '车辆ID',

    pickup_store    BIGINT        NOT NULL                  COMMENT '取车门店',
    return_store    BIGINT        NOT NULL                  COMMENT '还车门店',
    pickup_time     DATETIME      NOT NULL                  COMMENT '取车时间',
    return_time     DATETIME      NOT NULL                  COMMENT '还车时间',
    actual_pickup   DATETIME      DEFAULT NULL              COMMENT '实际取车',
    actual_return   DATETIME      DEFAULT NULL              COMMENT '实际还车',

    daily_price     DECIMAL(10,2) NOT NULL                  COMMENT '日租金',
    days            INT           NOT NULL                  COMMENT '租赁天数',
    total           DECIMAL(10,2) NOT NULL                  COMMENT '租金总额',
    deposit         DECIMAL(10,2) NOT NULL DEFAULT 0        COMMENT '押金',
    paid            DECIMAL(10,2) NOT NULL DEFAULT 0        COMMENT '实付金额',

    status          TINYINT       NOT NULL DEFAULT 0        COMMENT '状态: 0待付 1已付 2租赁中 3已完成 4已取消 5超时 6已退款',

    pickup_battery  DECIMAL(5,2)  DEFAULT NULL              COMMENT '取车电量',
    return_battery  DECIMAL(5,2)  DEFAULT NULL              COMMENT '还车电量',
    pickup_mileage  DECIMAL(10,2) DEFAULT NULL              COMMENT '取车里程',
    return_mileage  DECIMAL(10,2) DEFAULT NULL              COMMENT '还车里程',

    cancel_reason   VARCHAR(200)  DEFAULT NULL              COMMENT '取消原因',
    remark          VARCHAR(500)  DEFAULT NULL              COMMENT '备注',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT       NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user (user_id),
    KEY idx_vehicle (vehicle_id),
    KEY idx_status (status),
    KEY idx_create (create_time)
) COMMENT='订单表';


-- ====================== 6. 支付记录表 ======================
CREATE TABLE payment (
    id              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
    pay_no          VARCHAR(30)   NOT NULL                  COMMENT '支付流水号',
    order_id        BIGINT        NOT NULL                  COMMENT '订单ID',
    order_no        VARCHAR(30)   NOT NULL                  COMMENT '订单号',
    user_id         BIGINT        NOT NULL                  COMMENT '用户ID',
    amount          DECIMAL(10,2) NOT NULL                  COMMENT '金额',
    pay_type        TINYINT       NOT NULL                  COMMENT '支付方式: 1支付宝 2微信 3银行卡',
    status          TINYINT       NOT NULL DEFAULT 0        COMMENT '状态: 0待付 1成功 2失败 3退款',
    pay_time        DATETIME      DEFAULT NULL              COMMENT '支付时间',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT       NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uk_pay_no (pay_no),
    KEY idx_order (order_id)
) COMMENT='支付记录表';


-- ====================== 6.5 押金记录表 ======================
CREATE TABLE deposit_record (
    id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    order_id        BIGINT       NOT NULL                  COMMENT '订单ID',
    order_no        VARCHAR(32)  NOT NULL                  COMMENT '订单编号',
    user_id         BIGINT       NOT NULL                  COMMENT '用户ID',
    vehicle_id      BIGINT       NOT NULL                  COMMENT '车辆ID',
    amount          DECIMAL(10,2) NOT NULL                 COMMENT '押金金额',
    status          TINYINT      NOT NULL DEFAULT 0        COMMENT '状态: 0待支付 1冻结 2已退',
    freeze_time     DATETIME     DEFAULT NULL              COMMENT '冻结时间',
    refund_time     DATETIME     DEFAULT NULL              COMMENT '退款时间',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user (user_id),
    KEY idx_status (status)
) COMMENT='押金记录表';


-- ====================== 6.6 押金退款表 ======================
CREATE TABLE deposit_refund (
    id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    deposit_id      BIGINT       NOT NULL                  COMMENT '押金记录ID',
    order_id        BIGINT       NOT NULL                  COMMENT '订单ID',
    order_no        VARCHAR(32)  NOT NULL                  COMMENT '订单编号',
    user_id         BIGINT       NOT NULL                  COMMENT '用户ID',
    amount          DECIMAL(10,2) NOT NULL                 COMMENT '退款金额',
    refund_type     TINYINT      NOT NULL DEFAULT 1        COMMENT '退款方式: 1原路退回 2线下转账',
    refund_reason   VARCHAR(500) DEFAULT NULL              COMMENT '退款原因',
    operator_id     BIGINT       DEFAULT NULL              COMMENT '操作人ID',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    KEY idx_deposit (deposit_id),
    KEY idx_order (order_id)
) COMMENT='押金退款表';


-- ====================== 7. 充电记录表 ======================
CREATE TABLE credit_log (
    id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    user_id         BIGINT       NOT NULL                  COMMENT '用户ID',
    type            TINYINT      NOT NULL                  COMMENT '类型: 1加分 2扣分',
    value           INT          NOT NULL                  COMMENT '分值',
    before_score    INT          NOT NULL                  COMMENT '变更前',
    after_score     INT          NOT NULL                  COMMENT '变更后',
    reason          VARCHAR(200) NOT NULL                  COMMENT '原因',
    order_no        VARCHAR(30)  DEFAULT NULL              COMMENT '关联订单',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    KEY idx_user (user_id)
) COMMENT='信用记录表';


-- ====================== 8. 调度表 ======================
CREATE TABLE dispatch (
    id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    vehicle_id      BIGINT       NOT NULL                  COMMENT '车辆ID',
    from_store      BIGINT       NOT NULL                  COMMENT '源门店',
    to_store        BIGINT       NOT NULL                  COMMENT '目标门店',
    reason          VARCHAR(200) DEFAULT NULL              COMMENT '原因',
    status          TINYINT      NOT NULL DEFAULT 0        COMMENT '状态: 0待发 1运输中 2已到 3取消',
    operator_id     BIGINT       NOT NULL                  COMMENT '操作人',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    KEY idx_vehicle (vehicle_id)
) COMMENT='调度表';


-- ====================== 9. 维修表 ======================
CREATE TABLE repair (
    id              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
    vehicle_id      BIGINT        NOT NULL                  COMMENT '车辆ID',
    type            TINYINT       NOT NULL DEFAULT 1        COMMENT '类型: 1保养 2故障 3事故',
    fault_desc      VARCHAR(500)  NOT NULL                  COMMENT '故障描述',
    cost            DECIMAL(10,2) NOT NULL DEFAULT 0        COMMENT '费用',
    status          TINYINT       NOT NULL DEFAULT 0        COMMENT '状态: 0待修 1维修中 2完成',
    operator_id     BIGINT        NOT NULL                  COMMENT '操作人',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT       NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    KEY idx_vehicle (vehicle_id)
) COMMENT='维修表';


-- ====================== 10. 充电记录表 ======================
CREATE TABLE charge_log (
    id              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
    vehicle_id      BIGINT        NOT NULL                  COMMENT '车辆ID',
    station         VARCHAR(100)  DEFAULT NULL              COMMENT '充电站',
    start_battery   DECIMAL(5,2)  NOT NULL                  COMMENT '开始电量',
    end_battery     DECIMAL(5,2)  DEFAULT NULL              COMMENT '结束电量',
    start_time      DATETIME      NOT NULL                  COMMENT '开始时间',
    end_time        DATETIME      DEFAULT NULL              COMMENT '结束时间',
    cost            DECIMAL(10,2) NOT NULL DEFAULT 0        COMMENT '费用',
    status          TINYINT       NOT NULL DEFAULT 0        COMMENT '状态: 0充电中 1完成',
    operator_id     BIGINT        NOT NULL                  COMMENT '操作人',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT       NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    KEY idx_vehicle (vehicle_id)
) COMMENT='充电记录表';


-- ====================== 11. 公告表 ======================
CREATE TABLE notice (
    id              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
    title           VARCHAR(200)  NOT NULL                  COMMENT '标题',
    content         TEXT          NOT NULL                  COMMENT '内容',
    type            TINYINT       NOT NULL DEFAULT 1        COMMENT '类型: 1系统 2活动 3维护',
    publisher_id    BIGINT        NOT NULL                  COMMENT '发布人',
    is_top          TINYINT       NOT NULL DEFAULT 0        COMMENT '是否置顶',
    view_count      INT           NOT NULL DEFAULT 0        COMMENT '浏览数',
    status          TINYINT       NOT NULL DEFAULT 0        COMMENT '状态: 0草稿 1已发',
    publish_time    DATETIME      DEFAULT NULL              COMMENT '发布时间',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT       NOT NULL DEFAULT 0,

    PRIMARY KEY (id)
) COMMENT='公告表';


-- ====================== 12. 操作日志表 ======================
CREATE TABLE op_log (
    id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    user_id         BIGINT       DEFAULT NULL              COMMENT '用户ID',
    username        VARCHAR(50)  DEFAULT NULL              COMMENT '用户名',
    module          VARCHAR(50)  NOT NULL                  COMMENT '模块',
    operation       VARCHAR(100) NOT NULL                  COMMENT '操作',
    ip              VARCHAR(50)  DEFAULT NULL              COMMENT 'IP地址',
    time            BIGINT       DEFAULT NULL              COMMENT '耗时ms',
    status          TINYINT      NOT NULL DEFAULT 1        COMMENT '状态: 0失败 1成功',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY idx_user (user_id),
    KEY idx_create (create_time)
) COMMENT='操作日志表';


-- ====================== 初始数据 ======================

-- 管理员 (密码: admin123)
INSERT INTO user (username, password, real_name, phone, credit, role, status) VALUES
('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '管理员', '13800000000', 100, 'ADMIN', 1);

-- 门店
INSERT INTO store (name, province, city, address, longitude, latitude, phone, manager) VALUES
('北京朝阳店', '北京', '北京', '朝阳区建国路88号',   116.468, 39.915, '010-88880001', '张伟'),
('上海浦东店', '上海', '上海', '浦东新区陆家嘴1000号', 121.502, 31.237, '021-68880002', '李娜'),
('深圳南山店', '广东', '深圳', '南山区科技园深南大道', 113.947, 22.536, '0755-86660003', '王强'),
('广州天河店', '广东', '广州', '天河区体育西路191号', 113.325, 23.137, '020-38880004', '陈芳'),
('杭州西湖店', '浙江', '杭州', '拱墅区武林广场21号',  120.170, 30.274, '0571-87770005', '赵明');

-- 车辆
INSERT INTO vehicle (brand, model, type, plate, color, seats, battery_cap, range_km, fast_charge, cur_battery, price, deposit, status, store_id, description) VALUES
('比亚迪', '汉EV',   1, '京A·0001', '黑色',  5, 85.44,  605, 0.5, 95,  299, 3000, 0, 1, '旗舰轿车'),
('比亚迪', '唐EV',   2, '京A·0002', '白色',  7, 108.8,  600, 0.6, 88,  359, 3500, 0, 1, '七座SUV'),
('比亚迪', '海豹',   1, '沪A·0003', '蓝色',  5, 82.56,  700, 0.4, 100, 269, 2500, 0, 2, '运动轿跑'),
('特斯拉', 'Model 3',1, '沪A·0004', '灰色',  5, 60,     556, 0.5, 72,  329, 3000, 0, 2, '经典车型'),
('特斯拉', 'Model Y',2, '粤B·0005', '白色',  5, 78.4,   640, 0.5, 90,  399, 4000, 0, 3, '热销SUV'),
('蔚来',   'ES6',    2, '粤B·0006', '深空蓝',5, 100,    610, 0.6, 65,  459, 5000, 2, 3, '智能SUV'),
('蔚来',   'ET5',    1, '粤A·0007', '极光绿',5, 100,    710, 0.5, 100, 389, 4000, 0, 4, '中型轿车'),
('小鹏',   'P7',     1, '粤A·0008', '红色',  5, 80.87,  670, 0.4, 82,  289, 2500, 0, 4, '智能轿跑'),
('小鹏',   'G9',     2, '浙A·0009', '黑色',  5, 98,     702, 0.2, 45,  429, 4500, 4, 5, '旗舰SUV'),
('理想',   'L7',     2, '浙A·0010', '银色',  5, 42.8,   1315,0.5, 92,  379, 3500, 0, 5, '增程SUV'),
('理想',   'L9',     2, '浙A·0011', '黑色',  6, 44.5,   1315,0.5, 78,  499, 5000, 1, 5, '旗舰SUV'),
('理想',   'MEGA',   3, '浙A·0012', '白色',  7, 102.7,  710, 0.4, 55,  559, 6000, 0, 5, '纯电MPV'),
('比亚迪', '宋Plus', 2, '京A·0013', '灰色',  5, 71.7,   505, 0.5, 80,  239, 2000, 0, 1, '家用SUV'),
('特斯拉', 'Model S',1, '沪A·0014', '红色',  5, 100,    715, 0.5, 88,  599, 6000, 0, 2, '旗舰轿车'),
('蔚来',   'ET7',    1, '粤A·0015', '远空蓝',5, 100,    1000,0.5, 95,  469, 5000, 0, 4, '旗舰轿车');

-- 车辆图片
INSERT INTO vehicle_img (vehicle_id, url, is_main, sort) VALUES
(1, '/upload/byd-han.jpg',  1, 1),
(2, '/upload/byd-tang.jpg', 1, 1),
(3, '/upload/byd-seal.jpg', 1, 1),
(4, '/upload/tesla-m3.jpg', 1, 1),
(5, '/upload/tesla-my.jpg', 1, 1);

-- 公告
INSERT INTO notice (title, content, type, publisher_id, is_top, status, publish_time) VALUES
('平台上线公告', '<p>新能源汽车租赁平台正式上线！支持比亚迪、特斯拉、蔚来、小鹏、理想五大品牌。</p>', 1, 1, 1, 1, NOW()),
('五一优惠活动', '<p>全场车辆日租8折！新用户首单减50元。</p>', 2, 1, 0, 1, NOW()),
('信用积分说明', '<p>正常完成订单+5分，超时还车-10分。信用分低于60限制租车。</p>', 1, 1, 0, 1, NOW());


-- ====================== 完成 ======================
SELECT '数据库初始化完成' AS msg;
SELECT COUNT(*) AS '表数量' FROM information_schema.tables WHERE table_schema = 'nev_rental';
