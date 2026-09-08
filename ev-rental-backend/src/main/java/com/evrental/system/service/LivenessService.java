package com.evrental.system.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 活体认证服务（模拟实现）
 *
 * ══════════════════════════════════════════════════════════════
 *  毕业设计说明：
 *  实际项目中，活体认证应调用第三方AI平台：
 *    - 百度AI人脸检测: https://ai.baidu.com/ai-doc/FACE/
 *    - 阿里云人脸核身: https://help.aliyun.com/document_detail/
 *    - 虹软ArcFace: https://ai.arcsoft.com.cn/
 *
 *  活体认证流程：
 *    1. 前端采集用户人脸照片（或录制眨眼/张嘴视频）
 *    2. 上传到后端
 *    3. 后端调用AI接口判断是否为真人
 *    4. 与身份证照片比对，判断是否为同一人
 *    5. 返回认证结果
 *
 *  本项目为演示效果，使用模拟认证。
 * ══════════════════════════════════════════════════════════════
 *
 * @author ev-rental
 */
@Slf4j
@Service
public class LivenessService {

    /** 认证动作类型 */
    public static final String[] ACTIONS = {"眨眼", "张嘴", "点头", "摇头"};

    /**
     * 生成随机活体动作指令
     * 前端根据指令引导用户完成动作
     *
     * @param count 动作数量（通常2-3个）
     * @return 动作指令列表
     */
    public String[] generateActions(int count) {
        String[] result = new String[count];
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < count; i++) {
            result[i] = ACTIONS[random.nextInt(ACTIONS.length)];
        }
        log.info("生成活体动作指令: {}", String.join(",", result));
        return result;
    }

    /**
     * 活体检测（验证是否为真人）
     *
     * @param faceImage  人脸照片Base64
     * @param actions    用户执行的动作列表
     * @return 检测结果
     */
    public Map<String, Object> livenessCheck(String faceImage, String[] actions) {
        log.info("开始活体检测...");

        // 模拟检测耗时
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Map<String, Object> result = new HashMap<>();

        // 模拟检测结果（实际应调用AI接口）
        // 真实场景会返回：face_detected, liveness_score, face_quality等
        boolean isLive = true;
        double livenessScore = 0.95 + Math.random() * 0.05;

        result.put("success", isLive);
        result.put("livenessScore", Math.round(livenessScore * 100) / 100.0);
        result.put("faceDetected", true);
        result.put("actionsCompleted", actions.length);
        result.put("message", isLive ? "活体检测通过" : "活体检测失败，请重试");

        log.info("活体检测完成: isLive={}, score={}", isLive, livenessScore);
        return result;
    }

    /**
     * 人脸比对（身份证照片 vs 活体照片）
     *
     * @param idCardImage 身份证照片Base64
     * @param liveImage   活体采集照片Base64
     * @return 比对结果
     */
    public Map<String, Object> faceCompare(String idCardImage, String liveImage) {
        log.info("开始人脸比对...");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Map<String, Object> result = new HashMap<>();

        // 模拟比对结果（实际应调用AI接口）
        double similarity = 0.85 + Math.random() * 0.15;
        boolean isSamePerson = similarity > 0.80;

        result.put("success", isSamePerson);
        result.put("similarity", Math.round(similarity * 100) / 100.0);
        result.put("threshold", 0.80);
        result.put("message", isSamePerson ? "人脸比对通过" : "人脸比对失败，非同一人");

        log.info("人脸比对完成: samePerson={}, similarity={}", isSamePerson, similarity);
        return result;
    }
}
