package com.douyin.backend.dto.video;

import java.util.List;

public record RecommendFeedResponse(
    List<VideoItemResponse> items,
    String cursor,
    boolean hasMore
) {
}
