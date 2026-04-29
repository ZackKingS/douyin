package com.douyin.backend.dto.search;

import com.douyin.backend.dto.user.UserInfoDto;
import java.util.List;

public record SearchUsers(
    List<UserInfoDto> items,
    int total
) {
}
