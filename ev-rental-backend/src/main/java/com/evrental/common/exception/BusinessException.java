package com.evrental.common.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 用于在业务逻辑中抛出自定义异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
