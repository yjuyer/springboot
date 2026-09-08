package com.evrental.system.controller;

import com.evrental.common.result.R;
import com.evrental.common.utils.RedisUtil;
import com.evrental.system.dto.LoginDTO;
import com.evrental.system.dto.RegisterDTO;
import com.evrental.system.entity.SysUser;
import com.evrental.system.mapper.SysUserMapper;
import com.evrental.system.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 认证接口
 * 登录、注册、忘记密码
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SysUserMapper userMapper;
    private final RedisUtil redisUtil;
    private final PasswordEncoder passwordEncoder;

    /** 登录 */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        return R.ok(authService.login(dto));
    }

    /** 注册（需要邮箱验证码） */
    @PostMapping("/register")
    public R<Void> register(@RequestBody RegisterDTO dto) {
        // 校验邮箱验证码
        String redisKey = "email:code:" + dto.getEmail();
        Object savedCode = redisUtil.get(redisKey);
        if (savedCode == null) {
            return R.error("邮箱验证码已过期，请重新发送");
        }
        if (!savedCode.toString().equals(dto.getEmailCode())) {
            return R.error("邮箱验证码错误");
        }

        authService.register(dto);

        // 注册成功后删除验证码
        redisUtil.delete(redisKey);
        return R.ok();
    }

    /** 获取当前用户 */
    @GetMapping("/info")
    public R<SysUser> info() {
        return R.ok(authService.getCurrentUser());
    }

    // ======================== 忘记密码 ========================

    /**
     * 发送验证码（忘记密码 - 手机号）
     */
    @PostMapping("/sendCode")
    public R<String> sendCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            return R.error("请输入正确的手机号");
        }

        // 校验手机号是否已注册
        SysUser user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getPhone, phone));
        if (user == null) {
            return R.error("该手机号未注册");
        }

        // 生成6位验证码
        String code = String.format("%06d", new Random().nextInt(999999));

        // 存入Redis，5分钟过期
        String redisKey = "sms:code:" + phone;
        redisUtil.set(redisKey, code, 5, TimeUnit.MINUTES);

        // 实际项目通过阿里云/腾讯云短信API发送
        // 这里直接返回验证码用于演示
        log.info("验证码已发送: phone={}, code={}", phone, code);

        return R.ok("验证码已发送（演示模式）", code);
    }

    /**
     * 发送邮箱验证码（注册用）
     *
     * 流程：
     * 1. 校验邮箱格式
     * 2. 校验邮箱是否已注册
     * 3. 生成6位验证码
     * 4. 存入Redis（5分钟过期）
     * 5. 模拟发送邮件（实际通过JavaMailSender发送）
     */
    @PostMapping("/sendEmailCode")
    public R<String> sendEmailCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            return R.error("请输入正确的邮箱地址");
        }

        // 校验邮箱是否已注册
        SysUser exist = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getEmail, email));
        if (exist != null) {
            return R.error("该邮箱已注册");
        }

        // 生成6位验证码
        String code = String.format("%06d", new Random().nextInt(999999));

        // 存入Redis，5分钟过期
        String redisKey = "email:code:" + email;
        redisUtil.set(redisKey, code, 5, TimeUnit.MINUTES);

        // 实际项目通过JavaMailSender发送邮件
        // 示例：spring.mail.host=smtp.qq.com
        //       spring.mail.username=xxx@qq.com
        //       spring.mail.password=授权码
        log.info("邮箱验证码已发送: email={}, code={}", email, code);

        return R.ok("验证码已发送（演示模式）", code);
    }

    /**
     * 校验验证码（手机）
     */
    @PostMapping("/verifyCode")
    public R<Void> verifyCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String code = params.get("code");

        String redisKey = "sms:code:" + phone;
        Object savedCode = redisUtil.get(redisKey);

        if (savedCode == null) {
            return R.error(400, "验证码已过期，请重新发送");
        }
        if (!savedCode.toString().equals(code)) {
            return R.error(400, "验证码错误");
        }

        return R.ok();
    }

    /**
     * 校验邮箱验证码
     */
    @PostMapping("/verifyEmailCode")
    public R<Void> verifyEmailCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String code = params.get("code");

        String redisKey = "email:code:" + email;
        Object savedCode = redisUtil.get(redisKey);

        if (savedCode == null) {
            return R.error(400, "验证码已过期，请重新发送");
        }
        if (!savedCode.toString().equals(code)) {
            return R.error(400, "验证码错误");
        }

        return R.ok();
    }

    /**
     * 重置密码
     *
     * 流程：
     * 1. 校验手机号 + 验证码
     * 2. 校验新密码格式
     * 3. 更新密码
     * 4. 删除验证码
     */
    @PostMapping("/resetPassword")
    public R<Void> resetPassword(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String code = params.get("code");
        String newPassword = params.get("newPassword");
        String confirmPassword = params.get("confirmPassword");

        // 校验验证码
        String redisKey = "sms:code:" + phone;
        Object savedCode = redisUtil.get(redisKey);
        if (savedCode == null) {
            return R.error("验证码已过期，请重新发送");
        }
        if (!savedCode.toString().equals(code)) {
            return R.error("验证码错误");
        }

        // 校验密码
        if (newPassword == null || newPassword.length() < 6) {
            return R.error("密码至少6位");
        }
        if (!newPassword.equals(confirmPassword)) {
            return R.error("两次密码不一致");
        }

        // 查找用户并更新密码
        SysUser user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getPhone, phone));
        if (user == null) {
            return R.error("用户不存在");
        }

        SysUser updateUser = new SysUser();
        updateUser.setId(user.getId());
        updateUser.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(updateUser);

        // 删除验证码
        redisUtil.delete(redisKey);

        log.info("密码重置成功: phone={}", phone);
        return R.ok();
    }
}
