package com.evrental.system.controller;

import com.evrental.common.result.R;
import com.evrental.common.security.LoginUser;
import com.evrental.common.utils.FileUtil;
import com.evrental.system.dto.RealNameDTO;
import com.evrental.system.entity.SysUser;
import com.evrental.system.mapper.SysUserMapper;
import com.evrental.system.service.OcrService;
import com.evrental.system.service.LivenessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户个人中心 + 实名认证
 *
 * 接口列表：
 *   GET  /api/user/profile          获取个人信息
 *   PUT  /api/user/profile          更新个人信息
 *   POST /api/user/uploadIdCard     上传身份证（OCR识别）
 *   POST /api/user/realNameVerify   提交实名认证
 *   POST /api/user/livenessActions  获取活体动作指令
 *   POST /api/user/livenessCheck    活体检测
 *   POST /api/user/fullVerify       完整实名认证（身份证+活体）
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final SysUserMapper userMapper;
    private final FileUtil fileUtil;
    private final OcrService ocrService;
    private final LivenessService livenessService;

    // ======================== 基本信息 ========================

    @GetMapping("/profile")
    public R<SysUser> profile(@AuthenticationPrincipal LoginUser user) {
        return R.ok(userMapper.selectById(user.getUserId()));
    }

    @PutMapping("/profile")
    public R<Void> updateProfile(@AuthenticationPrincipal LoginUser user,
                                 @RequestBody Map<String, String> params) {
        SysUser u = new SysUser();
        u.setId(user.getUserId());
        if (params.containsKey("phone")) u.setPhone(params.get("phone"));
        if (params.containsKey("email")) u.setEmail(params.get("email"));
        if (params.containsKey("avatar")) u.setAvatar(params.get("avatar"));
        if (params.containsKey("city")) u.setCity(params.get("city"));
        if (params.containsKey("address")) u.setAddress(params.get("address"));
        userMapper.updateById(u);
        return R.ok();
    }

    // ======================== 实名认证 ========================

    /**
     * 上传身份证照片，OCR自动识别
     *
     * 流程：
     * 1. 接收身份证正面照片
     * 2. 保存到服务器
     * 3. 调用OCR识别姓名和身份证号
     * 4. 返回识别结果，让用户确认
     */
    @PostMapping("/uploadIdCard")
    public R<Map<String, String>> uploadIdCard(
            @AuthenticationPrincipal LoginUser user,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "side", defaultValue = "front") String side) throws IOException {

        // 1. 校验文件
        if (file.isEmpty()) {
            return R.error("请选择身份证照片");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            return R.error("文件大小不能超过2MB");
        }

        // 2. 保存文件
        String filePath = fileUtil.upload(file);
        log.info("身份证{}面已上传: userId={}, path={}", side, user.getUserId(), filePath);

        // 3. 保存到用户记录
        SysUser u = new SysUser();
        u.setId(user.getUserId());
        if ("front".equals(side)) {
            u.setIdCardFront(filePath);
        } else {
            u.setIdCardBack(filePath);
        }
        userMapper.updateById(u);

        // 4. 调用OCR识别（模拟）
        Map<String, String> ocrResult = new HashMap<>();
        if ("front".equals(side)) {
            // 正面：识别姓名、身份证号
            Map<String, String> ocr = ocrService.recognize(filePath);
            ocrResult.put("name", ocr.get("name"));
            ocrResult.put("idCard", ocr.get("idCard"));
            ocrResult.put("address", ocr.get("address"));
            ocrResult.put("birth", ocr.get("birth"));
            ocrResult.put("idCardFront", filePath);
        } else {
            // 背面：识别签发机关、有效期
            ocrResult.put("idCardBack", filePath);
            ocrResult.put("issueOrg", "北京市公安局朝阳分局");
            ocrResult.put("validDate", "2020.01.01-2040.01.01");
        }

        return R.ok(ocrResult);
    }

    /**
     * 提交实名认证
     *
     * @param dto 包含姓名和身份证号
     */
    @PostMapping("/realNameVerify")
    public R<Void> realNameVerify(@AuthenticationPrincipal LoginUser user,
                                  @RequestBody RealNameDTO dto) {
        // 1. 校验姓名格式
        if (!ocrService.validateName(dto.getRealName())) {
            return R.error("姓名格式不正确（需2-20个汉字）");
        }

        // 2. 校验身份证号格式
        if (!ocrService.validateIdCard(dto.getIdCard())) {
            return R.error("身份证号格式不正确");
        }

        // 3. 更新用户实名信息（idCardVerified保持0，等待管理员审核）
        SysUser u = new SysUser();
        u.setId(user.getUserId());
        u.setRealName(dto.getRealName());
        u.setIdCard(dto.getIdCard());
        u.setIdCardVerified(0); // 已上传，待审核
        userMapper.updateById(u);

        log.info("实名认证提交: userId={}, name={}", user.getUserId(), dto.getRealName());
        return R.ok();
    }

    // ======================== 活体认证 ========================

    /**
     * 获取活体检测动作指令
     *
     * 前端根据指令引导用户完成：眨眼、张嘴、点头等动作
     */
    @PostMapping("/livenessActions")
    public R<String[]> getLivenessActions() {
        String[] actions = livenessService.generateActions(3);
        return R.ok(actions);
    }

    /**
     * 活体检测
     *
     * @param faceImage 人脸照片Base64
     * @param actions   用户执行的动作
     */
    @PostMapping("/livenessCheck")
    public R<Map<String, Object>> livenessCheck(@AuthenticationPrincipal LoginUser user,
                                                @RequestBody Map<String, Object> params) {
        String faceImage = (String) params.get("faceImage");
        @SuppressWarnings("unchecked")
        java.util.List<String> actionList = (java.util.List<String>) params.get("actions");

        String[] actions = actionList != null ?
                actionList.toArray(new String[0]) : new String[0];

        Map<String, Object> result = livenessService.livenessCheck(faceImage, actions);
        return R.ok(result);
    }

    /**
     * 完整实名认证（身份证OCR + 活体检测 + 人脸比对）
     *
     * 一步完成全部认证流程
     */
    @PostMapping("/fullVerify")
    public R<Map<String, Object>> fullVerify(@AuthenticationPrincipal LoginUser user,
                                             @RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();

        String realName = params.get("realName");
        String idCard = params.get("idCard");
        String idCardImage = params.get("idCardImage"); // Base64
        String liveImage = params.get("liveImage");     // Base64

        // 1. 校验姓名
        if (!ocrService.validateName(realName)) {
            return R.error("姓名格式不正确");
        }

        // 2. 校验身份证号
        if (!ocrService.validateIdCard(idCard)) {
            return R.error("身份证号格式不正确");
        }

        // 3. 活体检测
        Map<String, Object> livenessResult = livenessService.livenessCheck(liveImage, new String[]{"眨眼"});
        if (!Boolean.TRUE.equals(livenessResult.get("success"))) {
            return R.error("活体检测失败，请重试");
        }

        // 4. 人脸比对
        Map<String, Object> compareResult = livenessService.faceCompare(idCardImage, liveImage);
        if (!Boolean.TRUE.equals(compareResult.get("success"))) {
            return R.error("人脸比对失败，非同一人");
        }

        // 5. 更新用户信息
        SysUser u = new SysUser();
        u.setId(user.getUserId());
        u.setRealName(realName);
        u.setIdCard(idCard);
        u.setIdCardVerified(1); // 已认证
        userMapper.updateById(u);

        result.put("verified", true);
        result.put("realName", realName);
        result.put("livenessScore", livenessResult.get("livenessScore"));
        result.put("similarity", compareResult.get("similarity"));
        result.put("message", "实名认证成功");

        log.info("完整实名认证成功: userId={}, name={}", user.getUserId(), realName);
        return R.ok(result);
    }

    /**
     * 上传驾驶证
     */
    @PostMapping("/uploadLicense")
    public R<String> uploadLicense(@AuthenticationPrincipal LoginUser user,
                                   @RequestParam("file") MultipartFile file) throws IOException {
        String path = fileUtil.upload(file);

        SysUser u = new SysUser();
        u.setId(user.getUserId());
        u.setDriverLicense(path);
        u.setLicenseVerified(1); // 待审核
        u.setLicenseUpdateTime(LocalDateTime.now());
        userMapper.updateById(u);

        return R.ok(path);
    }

    /**
     * 检查是否可以更改驾驶证（每半年一次）
     */
    @GetMapping("/license/canChange")
    public R<Map<String, Object>> canChangeLicense(@AuthenticationPrincipal LoginUser user) {
        Map<String, Object> result = new HashMap<>();
        SysUser sysUser = userMapper.selectById(user.getUserId());

        // 如果没有上传过驾驶证，可以上传
        if (sysUser.getDriverLicense() == null || sysUser.getDriverLicense().isEmpty()) {
            result.put("canChange", true);
            result.put("message", "可以上传驾驶证");
            return R.ok(result);
        }

        // 如果驾驶证被拒绝，可以重新上传
        if (sysUser.getLicenseVerified() != null && sysUser.getLicenseVerified() == 3) {
            result.put("canChange", true);
            result.put("message", "可以重新上传驾驶证");
            return R.ok(result);
        }

        // 检查是否在半年内更改过
        LocalDateTime lastUpdate = sysUser.getLicenseUpdateTime();
        if (lastUpdate != null) {
            LocalDateTime sixMonthsLater = lastUpdate.plusMonths(6);
            if (LocalDateTime.now().isBefore(sixMonthsLater)) {
                result.put("canChange", false);
                result.put("message", "每半年只能更改一次驾驶证，下次可更改时间：" + sixMonthsLater.toLocalDate());
                return R.ok(result);
            }
        }

        // 可以更改
        result.put("canChange", true);
        result.put("message", "可以更改驾驶证");
        return R.ok(result);
    }
}
