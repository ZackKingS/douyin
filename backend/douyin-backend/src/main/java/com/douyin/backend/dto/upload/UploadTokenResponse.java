package com.douyin.backend.dto.upload;

public record UploadTokenResponse(
    String uploadToken,
    String uploadUrl,
    String fileKey,
    long expiresIn
) {
}
