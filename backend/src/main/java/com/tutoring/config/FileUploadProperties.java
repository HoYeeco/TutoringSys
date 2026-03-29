package com.tutoring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "file")
public class FileUploadProperties {

    private String uploadDir = "./uploads";

    private Long maxFileSize = 10 * 1024 * 1024L;

    private String[] allowedImageTypes = {"jpg", "jpeg", "png", "gif", "webp", "bmp"};

}
