package com.tutoring.controller;

import com.tutoring.common.Result;
import com.tutoring.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "文件上传", description = "文件上传相关接口")
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @Operation(summary = "上传图片", description = "上传图片文件，支持jpg、jpeg、png、gif、webp、bmp格式，最大10MB")
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadImage(file);
        
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        result.put("filename", file.getOriginalFilename());
        result.put("size", String.valueOf(file.getSize()));
        
        return Result.success(result);
    }

    @Operation(summary = "上传文件", description = "上传文件到指定分类目录")
    @PostMapping("/file")
    public Result<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "files") String category) {
        String url = fileUploadService.uploadFile(file, category);
        
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        result.put("filename", file.getOriginalFilename());
        result.put("size", String.valueOf(file.getSize()));
        
        return Result.success(result);
    }

}
