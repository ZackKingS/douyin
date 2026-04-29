package com.douyin.backend.dto.search;

import com.douyin.backend.dto.video.TopicInfoDto;
import java.util.List;

public record SearchTopics(
    List<TopicInfoDto> items,
    int total
) {
}
