package com.douyin.backend.dto.video;

public record LikeResponse(
    boolean isLiked,
    long likeCount
) {
}
