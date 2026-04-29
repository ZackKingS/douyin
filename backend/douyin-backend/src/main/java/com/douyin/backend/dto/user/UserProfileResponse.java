package com.douyin.backend.dto.user;

public record UserProfileResponse(
    Long userId,
    String username,
    String nickname,
    String avatar,
    Integer gender,
    String birthday,
    String signature,
    String country,
    String province,
    String city,
    long followCount,
    long fansCount,
    long likeCount,
    long videoCount,
    boolean isFollowing,
    boolean isFollowed,
    boolean isMutual,
    int level,
    String badge,
    String createTime
) {
}
