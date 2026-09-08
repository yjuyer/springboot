package com.evrental.common.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录用户信息
 * 存储在SecurityContextHolder中，供Controller直接获取
 */
@Data
@AllArgsConstructor
public class LoginUser {

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 角色编码: ADMIN / OPERATOR / USER */
    private String role;
}
