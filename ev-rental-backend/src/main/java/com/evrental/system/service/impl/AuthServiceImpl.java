package com.evrental.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evrental.common.exception.BusinessException;
import com.evrental.common.security.JwtUtil;
import com.evrental.common.security.LoginUser;
import com.evrental.system.dto.LoginDTO;
import com.evrental.system.dto.RegisterDTO;
import com.evrental.system.entity.SysUser;
import com.evrental.system.mapper.SysUserMapper;
import com.evrental.system.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 认证服务 - 匹配sys_user表结构
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 角色默认USER（可通过关联查询扩展）
        String role = "USER";
        if ("admin".equals(user.getUsername())) {
            role = "ADMIN";
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("role", role);
        result.put("avatar", user.getAvatar());
        return result;
    }

    @Override
    @Transactional
    public void register(RegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次密码不一致");
        }
        long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setCreditScore(100);
        user.setIdCardVerified(0);
        user.setLicenseVerified(0);
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public SysUser getCurrentUser() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userMapper.selectById(loginUser.getUserId());
    }
}
