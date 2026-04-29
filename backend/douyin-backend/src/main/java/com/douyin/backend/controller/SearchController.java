package com.douyin.backend.controller;

import com.douyin.backend.auth.CurrentUser;
import com.douyin.backend.common.ApiResponse;
import com.douyin.backend.dto.search.SearchHotResponse;
import com.douyin.backend.dto.search.SearchResponse;
import com.douyin.backend.service.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ApiResponse<SearchResponse> search(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "all") String type,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size,
        @CurrentUser Long currentUserId
    ) {
        return ApiResponse.success(searchService.search(keyword, page, size, currentUserId));
    }

    @GetMapping("/hot")
    public ApiResponse<SearchHotResponse> hot() {
        return ApiResponse.success(searchService.hot());
    }
}
