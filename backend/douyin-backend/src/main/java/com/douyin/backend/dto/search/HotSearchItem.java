package com.douyin.backend.dto.search;

public record HotSearchItem(
    int rank,
    String word,
    long hotValue,
    String hotTrend,
    long searchCount
) {
}
