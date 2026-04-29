package com.douyin.backend.dto.video;

import java.util.List;

public record CommentListResponse(
    List<CommentItemResponse> items,
    int page,
    int size,
    int total,
    boolean hasMore
) {
}
