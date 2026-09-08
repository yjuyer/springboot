package com.evrental.common.utils;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileUtil {

    @Value("${file.upload-path:D:/ev-rental/upload/}")
    private String uploadPath;

    public String upload(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        Path dir = Paths.get(uploadPath);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : ".png";
        String fileName = IdUtil.fastSimpleUUID() + ext;

        File dest = new File(uploadPath + fileName);
        file.transferTo(dest);

        return "/upload/" + fileName;
    }
}