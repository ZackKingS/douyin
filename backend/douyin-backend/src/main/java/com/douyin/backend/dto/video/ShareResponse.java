package com.douyin.backend.dto.video;

public record ShareResponse(
    String shareUrl,
    long shareCount
) {
}
