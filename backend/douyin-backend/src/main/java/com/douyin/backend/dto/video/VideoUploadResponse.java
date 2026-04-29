package com.douyin.backend.dto.video;

public record VideoUploadResponse(
    String videoId,
    String title,
    String status,
    String coverUrl,
    String videoUrl
) {
}
