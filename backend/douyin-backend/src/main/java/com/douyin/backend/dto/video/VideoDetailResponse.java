package com.douyin.backend.dto.video;

import com.douyin.backend.dto.user.UserInfoDto;
import java.util.List;

public record VideoDetailResponse(
    String videoId,
    UserInfoDto author,
    String title,
    String description,
    String videoUrl,
    String coverUrl,
    long duration,
    int width,
    int height,
    long fileSize,
    long likeCount,
    long commentCount,
    long shareCount,
    long collectCount,
    long viewCount,
    boolean isLiked,
    boolean isCollected,
    boolean isFollowing,
    List<TopicInfoDto> topics,
    MusicInfoDto music,
    String location,
    Double latitude,
    Double longitude,
    String createTime
) {
}
