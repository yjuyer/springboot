package com.evrental.business.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evrental.business.entity.ImageBlob;
import com.evrental.business.mapper.ImageBlobMapper;
import org.springframework.stereotype.Service;

@Service
public class ImageBlobService extends ServiceImpl<ImageBlobMapper, ImageBlob> {

    /**
     * 保存图片到数据库
     */
    public Long saveImage(String fileName, String fileType, long fileSize, byte[] data) {
        ImageBlob blob = new ImageBlob();
        blob.setFileName(fileName);
        blob.setFileType(fileType);
        blob.setFileSize(fileSize);
        blob.setData(data);
        save(blob);
        return blob.getId();
    }

    /**
     * 根据ID获取图片
     */
    public ImageBlob getImage(Long id) {
        return getById(id);
    }
}
