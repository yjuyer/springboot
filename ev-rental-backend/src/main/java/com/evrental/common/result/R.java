package com.evrental.common.result;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一响应结果封装
 * 所有接口返回此格式
 *
 * @param <T> 数据类型
 */
@Data
public class R<T> implements Serializable {

    private int code;
    private String message;
    private T data;

    private R() {}

    /** 成功 - 无数据 */
    public static <T> R<T> ok() {
        return ok("操作成功", null);
    }

    /** 成功 - 带数据 */
    public static <T> R<T> ok(T data) {
        return ok("操作成功", data);
    }

    /** 成功 - 自定义消息 */
    public static <T> R<T> ok(String message, T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    /** 失败 */
    public static <T> R<T> error(String message) {
        return error(500, message);
    }

    /** 失败 - 自定义状态码 */
    public static <T> R<T> error(int code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
}
