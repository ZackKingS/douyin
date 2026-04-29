package com.douyin.backend.dto.video;

import java.util.List;

public record VideoListResponse(
    List<VideoItemResponse> items,
    int page,
    int size,
    int total,
    boolean hasMore
) {
}
