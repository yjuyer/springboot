-- 补充缺失的表 (user_coupon, vehicle_review, vehicle_dispatch, op_log, user_favorite, vehicle_image)

CREATE TABLE IF NOT EXISTS user_coupon (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    user_id     BIGINT   NOT NULL,
    coupon_id   BIGINT   NOT NULL,
    order_id    BIGINT   DEFAULT NULL,
    status      TINYINT  NOT NULL DEFAULT 0,
    get_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    use_time    DATETIME DEFAULT NULL,
    expire_time DATETIME DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT  NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_coupon_id (coupon_id),
    KEY idx_status (status),
    UNIQUE KEY uk_user_coupon (user_id, coupon_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

CREATE TABLE IF NOT EXISTS vehicle_review (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    order_id    BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    vehicle_id  BIGINT       NOT NULL,
    store_id    BIGINT       DEFAULT NULL,
    rating      TINYINT      NOT NULL,
    tags        VARCHAR(200) DEFAULT NULL,
    content     VARCHAR(1000) DEFAULT NULL,
    anonymous   TINYINT      NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 0,
    reply       VARCHAR(500) DEFAULT NULL,
    reply_time  DATETIME     DEFAULT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_review_order (order_id),
    KEY idx_vehicle_id (vehicle_id),
    KEY idx_user_id (user_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆评价表';

CREATE TABLE IF NOT EXISTS vehicle_dispatch (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    vehicle_id    BIGINT       NOT NULL,
    from_store_id BIGINT       NOT NULL,
    to_store_id   BIGINT       NOT NULL,
    reason        VARCHAR(300) DEFAULT NULL,
    status        TINYINT      NOT NULL DEFAULT 0,
    operator_id   BIGINT       DEFAULT NULL,
    start_time    DATETIME     DEFAULT NULL,
    complete_time DATETIME     DEFAULT NULL,
    remark        VARCHAR(500) DEFAULT NULL,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_vehicle_id (vehicle_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆调度记录表';

CREATE TABLE IF NOT EXISTS op_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       DEFAULT NULL,
    username    VARCHAR(50)  DEFAULT NULL,
    module      VARCHAR(50)  NOT NULL,
    operation   VARCHAR(100) NOT NULL,
    ip          VARCHAR(50)  DEFAULT NULL,
    time        BIGINT       DEFAULT NULL,
    status      TINYINT      NOT NULL DEFAULT 1,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user (user_id),
    KEY idx_create (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

CREATE TABLE IF NOT EXISTS user_favorite (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    user_id     BIGINT   NOT NULL,
    vehicle_id  BIGINT   NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_vehicle (user_id, vehicle_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

CREATE TABLE IF NOT EXISTS vehicle_image (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    vehicle_id  BIGINT       NOT NULL,
    image_url   VARCHAR(500) DEFAULT NULL,
    sort_num    INT          DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_vehicle_id (vehicle_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆图片表';
