package com.evrental.common.exception;

import com.evrental.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一捕获异常并返回R格式
 */
@Slf4j
@RestControllerAdvice
public class AppExceptionHandler {

    /** 业务异常 */
    @ExceptionHandler(BusinessException.class)
    public R<?> handleBusiness(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return R.error(e.getCode(), e.getMessage());
    }

    /** 参数校验异常 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<?> handleValid(MethodArgumentNotValidException e) {
        FieldError fe = e.getBindingResult().getFieldError();
        String msg = fe != null ? fe.getDefaultMessage() : "参数校验失败";
        return R.error(400, msg);
    }

    /** 参数绑定异常 */
    @ExceptionHandler(BindException.class)
    public R<?> handleBind(BindException e) {
        FieldError fe = e.getBindingResult().getFieldError();
        return R.error(400, fe != null ? fe.getDefaultMessage() : "参数绑定失败");
    }

    /** 权限不足 */
    @ExceptionHandler(AccessDeniedException.class)
    public R<?> handleDenied(AccessDeniedException e) {
        return R.error(403, "没有访问权限");
    }

    /** 认证失败 */
    @ExceptionHandler(BadCredentialsException.class)
    public R<?> handleAuth(BadCredentialsException e) {
        return R.error(401, "用户名或密码错误");
    }

    /** 其他异常 */
    @ExceptionHandler(Exception.class)
    public R<?> handleException(Exception e) {
        log.error("系统异常", e);
        return R.error("系统内部错误");
    }
}
