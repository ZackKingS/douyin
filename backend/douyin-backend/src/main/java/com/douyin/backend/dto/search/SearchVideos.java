package com.douyin.backend.dto.search;

import com.douyin.backend.dto.video.VideoItemResponse;
import java.util.List;

public record SearchVideos(
    List<VideoItemResponse> items,
    int total
) {
}
