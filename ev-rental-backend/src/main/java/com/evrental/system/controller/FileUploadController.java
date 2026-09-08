package com.evrental.system.controller;

import cn.hutool.core.util.IdUtil;
import com.evrental.common.result.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@RestController
public class FileUploadController {

    @Value("${file.upload-path}")
    private String uploadPath;

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".png"));
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @PostMapping("/api/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return R.error("上传文件不能为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return R.error("文件大小不能超过5MB");
        }

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return R.error("仅支持 jpg、png、jpeg 格式图片");
        }

        String fileName = IdUtil.fastSimpleUUID() + ext;

        File dest = new File(uploadPath + fileName);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        file.transferTo(dest);

        String url = "/upload/" + fileName;
        return R.ok(url);
    }

    @PostMapping("/api/file/upload")
    public R<String> fileUpload(@RequestParam("file") MultipartFile file) throws IOException {
        return upload(file);
    }
}
