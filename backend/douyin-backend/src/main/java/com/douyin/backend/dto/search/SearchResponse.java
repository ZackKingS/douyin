package com.douyin.backend.dto.search;

public record SearchResponse(
    String keyword,
    SearchUsers users,
    SearchVideos videos,
    SearchTopics topics,
    int page,
    int size,
    boolean hasMore
) {
}
