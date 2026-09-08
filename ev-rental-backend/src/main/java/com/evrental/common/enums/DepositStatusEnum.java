package com.evrental.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 押金状态枚举
 *
 *  待支付(0) → 押金冻结(1) → 押金已退(2)
 */
@Getter
@AllArgsConstructor
public enum DepositStatusEnum {

    PENDING_PAY(0, "待支付"),
    FROZEN(1, "押金冻结"),
    REFUNDED(2, "押金已退");

    private final int code;
    private final String desc;

    public static DepositStatusEnum of(int code) {
        for (DepositStatusEnum s : values()) {
            if (s.getCode() == code) return s;
        }
        throw new IllegalArgumentException("未知押金状态: " + code);
    }
}