package com.douyin.backend.controller;

import com.douyin.backend.auth.CurrentUser;
import com.douyin.backend.common.ApiResponse;
import com.douyin.backend.dto.video.RecommendFeedResponse;
import com.douyin.backend.service.RecommendService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    @GetMapping("/feed")
    public ApiResponse<RecommendFeedResponse> feed(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "home") String source,
        @RequestParam(required = false) String cursor,
        @CurrentUser Long currentUserId
    ) {
        return ApiResponse.success(recommendService.feed(page, size, currentUserId));
    }
}
