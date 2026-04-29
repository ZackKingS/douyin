package com.douyin.backend.dto.search;

import java.util.List;

public record SearchHotResponse(
    List<HotSearchItem> items,
    String updateTime
) {
}
