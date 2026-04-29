package com.douyin.backend.controller;

import com.douyin.backend.auth.CurrentUser;
import com.douyin.backend.auth.RequireLogin;
import com.douyin.backend.common.ApiResponse;
import com.douyin.backend.dto.upload.FileUploadResponse;
import com.douyin.backend.dto.upload.UploadTokenRequest;
import com.douyin.backend.dto.upload.UploadTokenResponse;
import com.douyin.backend.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload/token")
    public ApiResponse<UploadTokenResponse> uploadToken(@Valid @RequestBody UploadTokenRequest request) {
        return ApiResponse.success(fileStorageService.createUploadToken(request));
    }

    @RequireLogin
    @PostMapping("/upload")
    public ApiResponse<FileUploadResponse> upload(
        @RequestPart("file") MultipartFile file,
        @RequestPart("type") String type,
        @CurrentUser Long currentUserId
    ) {
        return ApiResponse.success("上传成功", fileStorageService.store(file, type, currentUserId));
    }
}
