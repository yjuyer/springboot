package com.evrental.business.controller;

import com.evrental.business.entity.ImageBlob;
import com.evrental.business.service.ImageBlobService;
import com.evrental.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 图片控制器 - 图片直接存入数据库
 */
@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageBlobService imageBlobService;

    private static final Set<String> ALLOWED_EXT = new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".png"));
    private static final long MAX_SIZE = 5 * 1024 * 1024;

    /**
     * 上传图片 → 存入数据库
     * 返回图片ID，前端用 /api/image/show/{id} 显示
     */
    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return R.error("上传文件不能为空");
        }
        if (file.getSize() > MAX_SIZE) {
            return R.error("文件大小不能超过5MB");
        }

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }
        if (!ALLOWED_EXT.contains(ext)) {
            return R.error("仅支持 jpg、png、jpeg 格式");
        }

        // 存入数据库
        Long id = imageBlobService.saveImage(
                originalName,
                file.getContentType(),
                file.getSize(),
                file.getBytes()
        );

        // 返回图片访问路径
        return R.ok("/api/image/show/" + id);
    }

    /**
     * 从数据库读取图片并显示
     */
    @GetMapping("/show/{id}")
    public ResponseEntity<byte[]> show(@PathVariable Long id) {
        ImageBlob blob = imageBlobService.getImage(id);
        if (blob == null || blob.getData() == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(blob.getFileType()));
        headers.setContentLength(blob.getData().length);
        headers.set("Cache-Control", "max-age=86400"); // 缓存1天

        return ResponseEntity.ok()
                .headers(headers)
                .body(blob.getData());
    }
}
