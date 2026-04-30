package com.douyin.backend.controller;

import com.douyin.backend.auth.CurrentUser;
import com.douyin.backend.auth.RequireLogin;
import com.douyin.backend.common.ApiResponse;
import com.douyin.backend.dto.video.*;
import com.douyin.backend.service.VideoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/feed")
    public ApiResponse<VideoFeedResponse> feed(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "0") long lastTime,
        @RequestParam(defaultValue = "home") String source,
        @CurrentUser Long currentUserId
    ) {
        return ApiResponse.success(videoService.getFeed(page, size, currentUserId));
    }

    @GetMapping("/{videoId}")
    public ApiResponse<VideoDetailResponse> detail(@PathVariable String videoId, @CurrentUser Long currentUserId) {
        return ApiResponse.success(videoService.getDetail(videoId, currentUserId));
    }

    @RequireLogin
    @PostMapping("/{videoId}/like")
    public ApiResponse<LikeResponse> like(@PathVariable String videoId, @CurrentUser Long currentUserId) {
        return ApiResponse.success("点赞成功", videoService.like(videoId, currentUserId));
    }

    @RequireLogin
    @DeleteMapping("/{videoId}/like")
    public ApiResponse<LikeResponse> unlike(@PathVariable String videoId, @CurrentUser Long currentUserId) {
        return ApiResponse.success("取消点赞成功", videoService.unlike(videoId, currentUserId));
    }

    @PostMapping("/{videoId}/share")
    public ApiResponse<ShareResponse> share(@PathVariable String videoId, @RequestParam(defaultValue = "copy") String platform) {
        return ApiResponse.success("分享成功", videoService.share(videoId));
    }

    @GetMapping("/{videoId}/comments")
    public ApiResponse<CommentListResponse> comments(
        @PathVariable String videoId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "popular") String sort
    ) {
        return ApiResponse.success(videoService.getComments(videoId, page, size));
    }

    @RequireLogin
    @PostMapping("/{videoId}/comments")
    public ApiResponse<CommentResponse> postComment(
        @PathVariable String videoId,
        @Valid @RequestBody CommentRequest request,
        @CurrentUser Long currentUserId
    ) {
        return ApiResponse.success("评论成功", videoService.postComment(videoId, request, currentUserId));
    }

    @RequireLogin
    @PostMapping
    public ApiResponse<VideoUploadResponse> upload(
        @CurrentUser Long currentUserId,
        @RequestPart("file") MultipartFile file,
        @RequestPart(value = "cover", required = false) MultipartFile cover,
        @RequestPart("title") String title,
        @RequestPart(value = "description", required = false) String description,
        @RequestPart(value = "topicIds", required = false) String topicIds,
        @RequestPart(value = "location", required = false) String location,
        @RequestPart(value = "latitude", required = false) Double latitude,
        @RequestPart(value = "longitude", required = false) Double longitude,
        @RequestPart(value = "atUserIds", required = false) String atUserIds,
        @RequestPart(value = "musicId", required = false) String musicId
    ) {
        return ApiResponse.success("上传成功", videoService.upload(
            currentUserId, file, cover, title, description, topicIds, location, latitude, longitude, atUserIds, musicId
        ));
    }
    @RequireLogin
    @DeleteMapping("/{videoId}")
    public ApiResponse<Void> delete(@PathVariable String videoId, @CurrentUser Long currentUserId) {
        videoService.delete(videoId, currentUserId);
        return ApiResponse.success("删除成功", null);
    }
}
