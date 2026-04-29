package com.douyin.backend.dto.auth;

public record LoginResponse(
    Long userId,
    String username,
    String nickname,
    String avatar,
    String token,
    String refreshToken,
    long expiresIn
) {
}
