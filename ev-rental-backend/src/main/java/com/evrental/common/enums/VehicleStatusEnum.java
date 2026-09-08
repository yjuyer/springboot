package com.evrental.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 车辆状态枚举
 */
@Getter
@AllArgsConstructor
public enum VehicleStatusEnum {

    IDLE(0, "空闲"),
    RESERVED(1, "已预约"),
    RENTING(2, "租赁中"),
    REPAIRING(3, "维修中"),
    CHARGING(4, "充电中"),
    DISPATCHING(5, "调度中");

    private final int code;
    private final String desc;
}
