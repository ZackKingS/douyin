package com.douyin.backend.service;

import com.douyin.backend.config.AppProperties;
import com.douyin.backend.dto.upload.FileUploadResponse;
import com.douyin.backend.dto.upload.UploadTokenRequest;
import com.douyin.backend.dto.upload.UploadTokenResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final AppProperties appProperties;

    public FileStorageService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public FileUploadResponse store(MultipartFile file, String type, Long userId) {
        try {
            String extension = getExtension(file.getOriginalFilename());
            String fileKey = buildFileKey(type, extension, userId);
            Path target = resolve(fileKey);
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return new FileUploadResponse(fileKey, appProperties.getBaseUrl() + "/media/" + fileKey);
        } catch (IOException ex) {
            throw new IllegalStateException("文件上传失败", ex);
        }
    }

    public FileUploadResponse storeGeneratedFile(Path source, String type, Long userId, String extension) {
        try {
            String cleanExtension = extension == null || extension.isBlank() ? "jpg" : extension;
            String fileKey = buildFileKey(type, cleanExtension, userId);
            Path target = resolve(fileKey);
            Files.createDirectories(target.getParent());
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            return new FileUploadResponse(fileKey, appProperties.getBaseUrl() + "/media/" + fileKey);
        } catch (IOException ex) {
            throw new IllegalStateException("生成文件保存失败", ex);
        }
    }

    public UploadTokenResponse createUploadToken(UploadTokenRequest request) {
        String extension = getExtension(request.getFilename());
        String fileKey = buildFileKey(request.getType(), extension, null);
        return new UploadTokenResponse(
            "upload_token_" + UUID.randomUUID().toString().replace("-", ""),
            appProperties.getBaseUrl() + "/api/v1/files/upload",
            fileKey,
            3600
        );
    }

    public Path resolve(String fileKey) {
        return Paths.get(appProperties.getStorageRoot()).toAbsolutePath().normalize().resolve(fileKey);
    }

    private String buildFileKey(String type, String extension, Long userId) {
        String datePath = LocalDate.now().toString().replace("-", "/");
        String name = UUID.randomUUID().toString().replace("-", "");
        return switch (type == null ? "" : type) {
            case "avatar" -> "images/users/" + (userId == null ? "guest" : userId) + "/avatar." + extension;
            case "cover" -> "covers/" + datePath + "/" + name + "." + extension;
            case "video" -> "videos/" + datePath + "/" + name + "." + extension;
            default -> "images/" + datePath + "/" + name + "." + extension;
        };
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "bin";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
