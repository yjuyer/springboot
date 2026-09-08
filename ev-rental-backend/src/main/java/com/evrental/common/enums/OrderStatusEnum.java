package com.evrental.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 *
 * ════════════════════════════════════════════════════════════════════════
 *  状态流转图
 * ════════════════════════════════════════════════════════════════════════
 *
 *     用户操作                           管理员/系统操作
 *     ──────                           ──────────────
 *
 *  创建订单
 *     │
 *  ┌──▼───────┐
 *  │  待支付    │
 *  │ PENDING  │
 *  └──┬───┬───┘
 *     │   │
 *     │   └──────────────┐
 *     │ 支付              │ 取消
 *     │                  │
 *  ┌──▼───────┐    ┌─────▼─────┐
 *  │  已支付    │    │  已取消    │
 *  │  PAID    │    │ CANCELLED │
 *  └──┬───────┘    └───────────┘
 *     │
 *     │ 押金冻结完成
 *     │
 *  ┌──▼──────────┐
 *  │   待取车     │
 *  │PENDING_PICKUP│
 *  └──┬──────────┘
 *     │
 *     │ 管理员确认取车
 *     │
 *  ┌──▼───────┐
 *  │  租赁中    │
 *  │ RENTING  │
 *  └──┬───────┘
 *     │
 *     │ 用户还车
 *     │
 *  ┌──▼──────────┐
 *  │   待还车     │
 *  │PENDING_RETURN│
 *  └──┬──────────┘
 *     │
 *     │ 管理员确认还车
 *     │
 *  ┌──▼───────┐
 *  │  已完成    │
 *  │COMPLETED │
 *  └──┬───────┘
 *     │
 *     │ 管理员发起退款
 *     │
 *  ┌──▼────────┐     ┌──────────┐
 *  │  退款中    │────→│  已退款   │
 *  │REFUNDING  │     │ REFUNDED │
 *  └───────────┘     └──────────┘
 *
 * ════════════════════════════════════════════════════════════════════════
 *
 * @author ev-rental
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    PENDING_PAY(0, "待支付"),
    PAID(1, "已支付"),
    PENDING_PICKUP(2, "待取车"),
    RENTING(3, "租赁中"),
    PENDING_RETURN(4, "待还车"),
    COMPLETED(5, "已完成"),
    CANCELLED(6, "已取消"),
    REFUNDING(7, "退款中"),
    REFUNDED(8, "已退款");

    private final int code;
    private final String desc;

    public static OrderStatusEnum of(int code) {
        for (OrderStatusEnum status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的订单状态: " + code);
    }
}