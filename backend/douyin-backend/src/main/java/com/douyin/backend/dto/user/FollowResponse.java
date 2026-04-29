package com.douyin.backend.dto.user;

public record FollowResponse(
    boolean isFollowing,
    long followCount,
    long fansCount
) {
}
