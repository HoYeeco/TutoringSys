package com.tutoring.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {

    String uploadImage(MultipartFile file);

    String uploadFile(MultipartFile file, String category);

    void deleteFile(String fileUrl);

}
