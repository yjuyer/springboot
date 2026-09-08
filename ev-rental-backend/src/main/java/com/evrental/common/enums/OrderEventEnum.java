package com.evrental.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单事件枚举
 * 定义所有能触发状态变更的操作
 */
@Getter
@AllArgsConstructor
public enum OrderEventEnum {

    PAY("支付", "USER"),

    CANCEL("取消订单", "USER"),

    FREEZE_DEPOSIT("押金冻结完成", "SYSTEM"),

    PICKUP("确认取车", "OPERATOR"),

    RETURN_REQUEST("申请还车", "USER"),

    RETURN_CONFIRM("确认还车", "OPERATOR"),

    REFUND_INIT("发起退款", "OPERATOR"),

    REFUND_COMPLETE("退款完成", "SYSTEM"),

    TIMEOUT("超时未取车", "SYSTEM");

    private final String desc;
    private final String actor;
}