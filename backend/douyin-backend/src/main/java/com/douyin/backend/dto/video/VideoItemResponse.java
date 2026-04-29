package com.douyin.backend.dto.video;

import com.douyin.backend.dto.user.UserInfoDto;

public record VideoItemResponse(
    String videoId,
    UserInfoDto author,
    String title,
    String description,
    String videoUrl,
    String coverUrl,
    long duration,
    int width,
    int height,
    long likeCount,
    long commentCount,
    long shareCount,
    long collectCount,
    long viewCount,
    boolean isLiked,
    boolean isCollected,
    String topicName,
    MusicInfoDto music,
    String location,
    String createTime,
    Long nextTime
) {
}
