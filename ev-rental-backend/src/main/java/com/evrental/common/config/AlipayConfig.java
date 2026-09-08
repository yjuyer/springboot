package com.evrental.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝沙箱配置
 *
 * ══════════════════════════════════════════════════════════════
 *  支付宝沙箱接入指南：
 *
 *  1. 登录支付宝开放平台 https://open.alipay.com
 *  2. 控制台 -> 沙箱 -> 创建沙箱应用
 *  3. 生成RSA2密钥对：
 *     - 下载支付宝开放平台开发助手
 *     - 生成密钥，复制应用公钥到沙箱配置
 *     - 保存应用私钥
 *  4. 配置沙箱：
 *     - APPID：沙箱应用的APPID
 *     - 应用私钥：生成的RSA私钥
 *     - 支付宝公钥：沙箱中查看
 *  5. 下载沙箱支付宝APP测试支付
 *
 *  沙箱环境地址：
 *    网关：https://openapi-sandbox.dl.alipaydev.com/gateway.do
 *    文档：https://opendocs.alipay.com/open/270/105899
 * ══════════════════════════════════════════════════════════════
 */
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {

    /** 应用ID（沙箱） */
    private String appId = "2021000123456789";

    /** 应用私钥 */
    private String privateKey = "";

    /** 支付宝公钥 */
    private String alipayPublicKey = "";

    /** 签名类型 */
    private String signType = "RSA2";

    /** 编码 */
    private String charset = "UTF-8";

    /** 沙箱网关 */
    private String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    /** 同步回调地址 */
    private String returnUrl = "http://localhost:5173/pay/success";

    /** 异步回调地址 */
    private String notifyUrl = "http://localhost:8080/api/pay/notify";

    /** 格式 */
    private String format = "json";

    /** 版本 */
    private String version = "1.0";
}
