package com.douyin.backend.dto.user;

import java.util.List;

public record UserListResponse(
    List<UserInfoDto> items,
    int page,
    int size,
    int total,
    boolean hasMore
) {
}
