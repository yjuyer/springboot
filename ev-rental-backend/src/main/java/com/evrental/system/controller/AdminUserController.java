package com.evrental.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evrental.business.service.NotificationService;
import com.evrental.common.result.R;
import com.evrental.system.entity.SysUser;
import com.evrental.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员 - 用户管理
 */
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final SysUserMapper userMapper;
    private final NotificationService notificationService;

    @GetMapping("/list")
    public R<IPage<SysUser>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        return R.ok(userMapper.selectPage(page,
                new LambdaQueryWrapper<SysUser>()
                        .like(username != null, SysUser::getUsername, username)
                        .like(phone != null, SysUser::getPhone, phone)
                        .eq(status != null, SysUser::getStatus, status)
                        .orderByDesc(SysUser::getCreateTime)));
    }

    @PutMapping("/status")
    public R<Void> status(@RequestBody Map<String, Object> params) {
        SysUser user = new SysUser();
        user.setId(Long.valueOf(params.get("id").toString()));
        user.setStatus(Integer.valueOf(params.get("status").toString()));
        userMapper.updateById(user);
        return R.ok();
    }

    @PutMapping("/resetPwd/{id}")
    public R<Void> resetPwd(@PathVariable Long id) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("123456"));
        userMapper.updateById(user);
        return R.ok();
    }

    // ======================== 实名认证审核 ========================

    /**
     * 获取待实名认证的用户列表
     */
    @GetMapping("/verify/list")
    public R<IPage<SysUser>> verifyList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer idCardVerified) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .isNotNull(SysUser::getIdCard)
                .ne(SysUser::getIdCard, "");
        if (idCardVerified != null) {
            wrapper.eq(SysUser::getIdCardVerified, idCardVerified);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        return R.ok(userMapper.selectPage(page, wrapper));
    }

    /**
     * 审核实名认证
     */
    @PutMapping("/verify/approve")
    public R<Void> approveVerify(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Integer status = Integer.valueOf(params.get("status").toString()); // 1通过 2拒绝

        SysUser user = new SysUser();
        user.setId(userId);
        user.setIdCardVerified(status);
        userMapper.updateById(user);

        // 发送实名认证审核结果通知
        if (status == 1) {
            notificationService.sendToUser(userId, "实名认证已通过",
                    "恭喜！您的实名认证已审核通过，现在可以正常使用租车服务。", 2, null);
        } else {
            notificationService.sendToUser(userId, "实名认证未通过",
                    "很抱歉，您的实名认证未通过审核，请检查信息后重新提交。", 2, null);
        }

        return R.ok();
    }

    // ======================== 驾驶证审核 ========================

    /**
     * 获取待驾驶证审核的用户列表
     */
    @GetMapping("/license/list")
    public R<IPage<SysUser>> licenseList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer licenseVerified) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .isNotNull(SysUser::getDriverLicense)
                .ne(SysUser::getDriverLicense, "");
        if (licenseVerified != null) {
            wrapper.eq(SysUser::getLicenseVerified, licenseVerified);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        return R.ok(userMapper.selectPage(page, wrapper));
    }

    /**
     * 审核驾驶证
     */
    @PutMapping("/license/approve")
    public R<Void> approveLicense(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Integer status = Integer.valueOf(params.get("status").toString()); // 2通过 3拒绝

        SysUser user = new SysUser();
        user.setId(userId);
        user.setLicenseVerified(status);
        userMapper.updateById(user);

        // 发送驾驶证审核结果通知
        if (status == 2) {
            notificationService.sendToUser(userId, "驾驶证审核已通过",
                    "恭喜！您的驾驶证已审核通过，现在可以正常租车。", 2, null);
        } else {
            notificationService.sendToUser(userId, "驾驶证审核未通过",
                    "很抱歉，您的驾驶证未通过审核，请检查证件信息后重新上传。", 2, null);
        }

        return R.ok();
    }

    /**
     * 删除用户实名认证（重置为未认证状态，需要重新审核）
     */
    @DeleteMapping("/verify/delete/{userId}")
    public R<Void> deleteVerify(@PathVariable Long userId) {
        SysUser user = new SysUser();
        user.setId(userId);
        // 清除实名认证信息
        user.setRealName(null);
        user.setIdCard(null);
        user.setIdCardFront(null);
        user.setIdCardBack(null);
        user.setIdCardVerified(0);
        userMapper.updateById(user);
        return R.ok();
    }

    /**
     * 删除用户驾驶证认证（重置为未上传状态，需要重新审核）
     */
    @DeleteMapping("/license/delete/{userId}")
    public R<Void> deleteLicense(@PathVariable Long userId) {
        SysUser user = new SysUser();
        user.setId(userId);
        // 清除驾驶证信息
        user.setDriverLicense(null);
        user.setLicenseVerified(0);
        user.setLicenseUpdateTime(null);
        userMapper.updateById(user);
        return R.ok();
    }
}
