package com.tutoring.service.impl;

import com.tutoring.config.FileUploadProperties;
import com.tutoring.exception.BusinessException;
import com.tutoring.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final FileUploadProperties fileUploadProperties;

    @Override
    public String uploadImage(MultipartFile file) {
        return uploadFile(file, "images");
    }

    @Override
    public String uploadFile(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }

        String fileExtension = getFileExtension(originalFilename);
        if (!isAllowedImageType(fileExtension)) {
            throw new BusinessException("不支持的文件类型，仅支持: " + Arrays.toString(fileUploadProperties.getAllowedImageTypes()));
        }

        if (file.getSize() > fileUploadProperties.getMaxFileSize()) {
            throw new BusinessException("文件大小超过限制，最大允许: " + (fileUploadProperties.getMaxFileSize() / 1024 / 1024) + "MB");
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + fileExtension;

        String relativePath = category + "/" + datePath + "/" + newFilename;

        String uploadDir = fileUploadProperties.getUploadDir();
        Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path uploadPath = basePath.resolve(relativePath);

        try {
            Files.createDirectories(uploadPath.getParent());
            file.transferTo(uploadPath.toFile());
            log.info("文件上传成功: {}", uploadPath);
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }

        return "/uploads/" + relativePath.replace("\\", "/");
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        if (!fileUrl.startsWith("/uploads/")) {
            return;
        }

        String relativePath = fileUrl.substring("/uploads/".length());
        Path filePath = Paths.get(fileUploadProperties.getUploadDir(), relativePath);

        try {
            Files.deleteIfExists(filePath);
            log.info("文件删除成功: {}", filePath);
        } catch (IOException e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    private boolean isAllowedImageType(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        return Arrays.stream(fileUploadProperties.getAllowedImageTypes())
            .anyMatch(type -> type.equalsIgnoreCase(extension));
    }

}
