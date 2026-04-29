package com.douyin.backend.controller;

import com.douyin.backend.auth.CurrentUser;
import com.douyin.backend.auth.RequireLogin;
import com.douyin.backend.common.ApiResponse;
import com.douyin.backend.dto.user.FollowResponse;
import com.douyin.backend.dto.user.UpdateUserRequest;
import com.douyin.backend.dto.user.UserListResponse;
import com.douyin.backend.dto.user.UserProfileResponse;
import com.douyin.backend.dto.video.VideoListResponse;
import com.douyin.backend.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserProfileResponse> getProfile(
        @PathVariable Long userId,
        @RequestParam(required = false) Long sourceUserId,
        @CurrentUser Long currentUserId
    ) {
        Long source = sourceUserId != null ? sourceUserId : currentUserId;
        return ApiResponse.success(userService.getProfile(userId, source));
    }

    @RequireLogin
    @PutMapping("/me")
    public ApiResponse<UserProfileResponse> updateMe(@CurrentUser Long currentUserId, @RequestBody UpdateUserRequest request) {
        return ApiResponse.success(userService.updateProfile(currentUserId, request));
    }

    @GetMapping("/{userId}/follows")
    public ApiResponse<UserListResponse> getFollows(@PathVariable Long userId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(userService.getFollows(userId, page, size));
    }

    @GetMapping("/{userId}/fans")
    public ApiResponse<UserListResponse> getFans(@PathVariable Long userId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(userService.getFans(userId, page, size));
    }

    @RequireLogin
    @PostMapping("/{userId}/follow")
    public ApiResponse<FollowResponse> follow(@PathVariable Long userId, @CurrentUser Long currentUserId) {
        return ApiResponse.success("关注成功", userService.follow(currentUserId, userId));
    }

    @RequireLogin
    @DeleteMapping("/{userId}/follow")
    public ApiResponse<FollowResponse> unfollow(@PathVariable Long userId, @CurrentUser Long currentUserId) {
        return ApiResponse.success("取消关注成功", userService.unfollow(currentUserId, userId));
    }

    @GetMapping("/{userId}/videos")
    public ApiResponse<VideoListResponse> getUserVideos(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "all") String type,
        @CurrentUser Long currentUserId
    ) {
        return ApiResponse.success(userService.getUserVideos(userId, page, size, currentUserId));
    }
}
