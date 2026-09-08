package com.evrental.business.service;

import cn.hutool.core.util.IdUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class QRCodeService {

    @Value("${file.upload-path}")
    private String uploadPath;

    private static final int WIDTH = 300;
    private static final int HEIGHT = 300;
    private static final String FORMAT = "PNG";
    private static final Color PRIMARY_COLOR = new Color(0x00, 0xB8, 0x94);
    private static final Color BG_COLOR = Color.WHITE;

    public String generateQRCode(String content) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);

            MatrixToImageConfig config = new MatrixToImageConfig(PRIMARY_COLOR.getRGB(), BG_COLOR.getRGB());
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(matrix, config);

            BufferedImage result = new BufferedImage(WIDTH, HEIGHT + 40, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = result.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2d.setColor(BG_COLOR);
            g2d.fillRect(0, 0, result.getWidth(), result.getHeight());

            g2d.drawImage(qrImage, 0, 0, null);

            g2d.setColor(new Color(0x33, 0x33, 0x33));
            g2d.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
            FontMetrics fm = g2d.getFontMetrics();
            String label = "扫码支付";
            int labelWidth = fm.stringWidth(label);
            g2d.drawString(label, (WIDTH - labelWidth) / 2, HEIGHT + 26);

            g2d.dispose();

            String fileName = "qrcode_" + IdUtil.fastSimpleUUID() + ".png";
            String dirPath = uploadPath + "qrcode/";
            Path dir = Paths.get(dirPath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            File outputFile = new File(dirPath + fileName);
            ImageIO.write(result, FORMAT, outputFile);

            String url = "/upload/qrcode/" + fileName;
            log.info("二维码生成成功: {}", url);
            return url;

        } catch (WriterException | IOException e) {
            log.error("二维码生成失败", e);
            throw new RuntimeException("二维码生成失败: " + e.getMessage());
        }
    }

    public byte[] generateQRCodeBytes(String content) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);

            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            ImageIO.write(image, FORMAT, baos);
            return baos.toByteArray();

        } catch (WriterException | IOException e) {
            log.error("二维码生成失败", e);
            throw new RuntimeException("二维码生成失败: " + e.getMessage());
        }
    }
}