package com.evrental.common.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成工具（基于ZXing）
 */
@Slf4j
@Component
public class QrCodeUtil {

    /**
     * 根据内容生成二维码Base64字符串
     *
     * @param content 二维码内容（URL/文本）
     * @param width   宽度
     * @param height  高度
     * @return Base64编码的PNG图片
     */
    public String generateBase64(String content, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", os);
            byte[] bytes = os.toByteArray();

            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            log.error("生成二维码失败", e);
            return null;
        }
    }

    /**
     * 生成二维码字节数组
     */
    public byte[] generateBytes(String content, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", os);
            return os.toByteArray();
        } catch (Exception e) {
            log.error("生成二维码失败", e);
            return new byte[0];
        }
    }
}
