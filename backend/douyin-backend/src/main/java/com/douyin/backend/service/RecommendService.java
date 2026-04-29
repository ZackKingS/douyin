package com.douyin.backend.service;

import com.douyin.backend.dto.video.RecommendFeedResponse;
import com.douyin.backend.dto.video.VideoFeedResponse;
import org.springframework.stereotype.Service;

@Service
public class RecommendService {

    private final VideoService videoService;

    public RecommendService(VideoService videoService) {
        this.videoService = videoService;
    }

    public RecommendFeedResponse feed(int page, int size, Long currentUserId) {
        VideoFeedResponse feed = videoService.getFeed(page, size, currentUserId);
        String cursor = feed.items().isEmpty() ? null : feed.items().get(feed.items().size() - 1).videoId();
        return new RecommendFeedResponse(feed.items(), cursor, feed.hasMore());
    }
}
