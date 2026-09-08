package com.evrental.system.service;

import com.evrental.system.dto.LoginDTO;
import com.evrental.system.dto.RegisterDTO;
import com.evrental.system.entity.SysUser;
import java.util.Map;

/**
 * 认证服务接口
 */
public interface AuthService {

    /** 用户登录，返回token等信息 */
    Map<String, Object> login(LoginDTO loginDTO);

    /** 用户注册 */
    void register(RegisterDTO registerDTO);

    /** 获取当前登录用户信息 */
    SysUser getCurrentUser();
}
