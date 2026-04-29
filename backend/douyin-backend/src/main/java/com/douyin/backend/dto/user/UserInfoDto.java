package com.douyin.backend.dto.user;

public record UserInfoDto(
    Long userId,
    String nickname,
    String avatar,
    String signature,
    long fansCount,
    long followCount,
    boolean isFollowing,
    boolean isMutual,
    int level
) {
}
