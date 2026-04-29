package com.douyin.backend.dto.video;

import java.util.List;

public record VideoFeedResponse(
    List<VideoItemResponse> items,
    Long nextTime,
    boolean hasMore
) {
}
