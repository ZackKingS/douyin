package com.douyin.backend.service;

import com.douyin.backend.common.BusinessException;
import com.douyin.backend.dto.upload.FileUploadResponse;
import com.douyin.backend.dto.user.UserInfoDto;
import com.douyin.backend.dto.video.*;
import com.douyin.backend.entity.Comment;
import com.douyin.backend.entity.User;
import com.douyin.backend.entity.Video;
import com.douyin.backend.entity.VideoLike;
import com.douyin.backend.repository.CommentRepository;
import com.douyin.backend.repository.FollowRepository;
import com.douyin.backend.repository.UserRepository;
import com.douyin.backend.repository.VideoLikeRepository;
import com.douyin.backend.repository.VideoRepository;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final VideoLikeRepository videoLikeRepository;
    private final CommentRepository commentRepository;
    private final FileStorageService fileStorageService;
    private final VideoCoverService videoCoverService;

    public VideoService(
        VideoRepository videoRepository,
        UserRepository userRepository,
        FollowRepository followRepository,
        VideoLikeRepository videoLikeRepository,
        CommentRepository commentRepository,
        FileStorageService fileStorageService,
        VideoCoverService videoCoverService
    ) {
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.videoLikeRepository = videoLikeRepository;
        this.commentRepository = commentRepository;
        this.fileStorageService = fileStorageService;
        this.videoCoverService = videoCoverService;
    }

    public VideoFeedResponse getFeed(int page, int size, Long currentUserId) {
        List<Video> videos = videoRepository.findByStatusOrderByCreateTimeDesc(1, PageRequest.of(page - 1, size));
        List<VideoItemResponse> items = videos.stream()
            .map(video -> VideoMapper.toVideoItem(video, currentUserId, userRepository, followRepository, videoLikeRepository))
            .toList();
        Long nextTime = items.isEmpty() ? null : items.get(items.size() - 1).nextTime();
        boolean hasMore = items.size() == size;
        return new VideoFeedResponse(items, nextTime, hasMore);
    }

    @Transactional
    public VideoDetailResponse getDetail(String videoId, Long currentUserId) {
        Video video = getByVideoId(videoId);
        video.setViewCount(video.getViewCount() + 1);
        videoRepository.save(video);
        return VideoMapper.toVideoDetail(video, currentUserId, userRepository, followRepository, videoLikeRepository);
    }

    @Transactional
    public LikeResponse like(String videoId, Long currentUserId) {
        Video video = getByVideoId(videoId);
        if (!videoLikeRepository.existsByUserIdAndVideoId(currentUserId, video.getId())) {
            VideoLike like = new VideoLike();
            like.setUserId(currentUserId);
            like.setVideoId(video.getId());
            videoLikeRepository.save(like);
            video.setLikeCount(video.getLikeCount() + 1);
            videoRepository.save(video);
            refreshAuthorLikeCount(video.getAuthorId());
        }
        return new LikeResponse(true, video.getLikeCount());
    }

    @Transactional
    public LikeResponse unlike(String videoId, Long currentUserId) {
        Video video = getByVideoId(videoId);
        videoLikeRepository.findByUserIdAndVideoId(currentUserId, video.getId()).ifPresent(like -> {
            videoLikeRepository.delete(like);
            video.setLikeCount(Math.max(0, video.getLikeCount() - 1));
            videoRepository.save(video);
        });
        refreshAuthorLikeCount(video.getAuthorId());
        return new LikeResponse(false, video.getLikeCount());
    }

    @Transactional
    public ShareResponse share(String videoId) {
        Video video = getByVideoId(videoId);
        video.setShareCount(video.getShareCount() + 1);
        videoRepository.save(video);
        return new ShareResponse("share/video/" + video.getVideoId(), video.getShareCount());
    }

    public CommentListResponse getComments(String videoId, int page, int size) {
        Video video = getByVideoId(videoId);
        List<Comment> topLevel = commentRepository.findByVideoIdAndStatusOrderByCreateTimeDesc(video.getId(), 1)
            .stream()
            .filter(item -> item.getParentId() == null)
            .toList();
        int total = topLevel.size();
        List<CommentItemResponse> items = topLevel.stream()
            .skip((long) (page - 1) * size)
            .limit(size)
            .map(this::toCommentItem)
            .toList();
        return new CommentListResponse(items, page, size, total, page * size < total);
    }

    @Transactional
    public CommentResponse postComment(String videoId, CommentRequest request, Long currentUserId) {
        Video video = getByVideoId(videoId);
        Comment comment = new Comment();
        comment.setVideoId(video.getId());
        comment.setUserId(currentUserId);
        comment.setContent(request.getContent());
        if (request.getParentId() != null && !request.getParentId().isBlank()) {
            Long parentDbId = parseCommentId(request.getParentId());
            comment.setParentId(parentDbId);
            comment.setRootId(parentDbId);
            commentRepository.findById(parentDbId).ifPresent(parent -> {
                parent.setReplyCount(parent.getReplyCount() + 1);
                commentRepository.save(parent);
            });
        }
        commentRepository.save(comment);
        video.setCommentCount(commentRepository.countByVideoIdAndStatus(video.getId(), 1));
        videoRepository.save(video);
        User user = userRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(404, "用户不存在"));
        UserInfoDto userInfo = new UserInfoDto(
            user.getId(),
            user.getNickname(),
            user.getAvatar(),
            user.getSignature(),
            user.getFansCount(),
            user.getFollowCount(),
            false,
            false,
            1
        );
        return new CommentResponse(
            "c_" + comment.getId(),
            userInfo,
            comment.getContent(),
            comment.getLikeCount(),
            comment.getReplyCount(),
            false,
            com.douyin.backend.util.TimeUtils.toIso(comment.getCreateTime())
        );
    }

    @Transactional
    public VideoUploadResponse upload(
        Long currentUserId,
        MultipartFile file,
        MultipartFile cover,
        String title,
        String description,
        String topicIds,
        String location,
        Double latitude,
        Double longitude,
        String atUserIds,
        String musicId
    ) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "视频文件不能为空");
        }
        FileUploadResponse storedVideo = fileStorageService.store(file, "video", currentUserId);
        String videoUrl = storedVideo.fileUrl();
        String coverUrl = resolveCoverUrl(cover, storedVideo, currentUserId);
        Video video = new Video();
        video.setVideoId("v_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        video.setAuthorId(currentUserId);
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoUrl(videoUrl);
        video.setCoverUrl(coverUrl);
        video.setDuration(30);
        video.setFileSize(file.getSize());
        video.setTopicIds(topicIds == null ? "" : topicIds);
        video.setLocation(location == null ? "" : location);
        video.setLatitude(latitude == null ? null : BigDecimal.valueOf(latitude));
        video.setLongitude(longitude == null ? null : BigDecimal.valueOf(longitude));
        video.setAtUserIds(atUserIds == null ? "" : atUserIds);
        video.setMusicId(musicId == null ? "" : musicId);
        videoRepository.save(video);
        User author = userRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(404, "用户不存在"));
        author.setVideoCount(videoRepository.countByAuthorId(currentUserId));
        userRepository.save(author);
        return new VideoUploadResponse(video.getVideoId(), video.getTitle(), "processing", video.getCoverUrl(), video.getVideoUrl());
    }

    @Transactional
    public void delete(String videoId, Long currentUserId) {
        Video video = getByVideoId(videoId);
        if (!video.getAuthorId().equals(currentUserId)) {
            throw new BusinessException(403, "只能删除自己发布的作品");
        }
        String videoUrl = video.getVideoUrl();
        String coverUrl = video.getCoverUrl();
        commentRepository.deleteByVideoId(video.getId());
        videoLikeRepository.deleteByVideoId(video.getId());
        videoRepository.delete(video);
        User author = userRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(404, "用户不存在"));
        author.setVideoCount(videoRepository.countByAuthorIdAndStatus(currentUserId, 1));
        author.setLikeCount(videoRepository.findByAuthorIdAndStatusOrderByCreateTimeDesc(currentUserId, 1)
            .stream()
            .mapToLong(Video::getLikeCount)
            .sum());
        userRepository.save(author);
        deleteStoredMedia(videoUrl, coverUrl);
    }

    private void deleteStoredMedia(String videoUrl, String coverUrl) {
        fileStorageService.deleteByFileUrl(videoUrl);
        if (coverUrl != null && !coverUrl.equals(videoUrl)) {
            fileStorageService.deleteByFileUrl(coverUrl);
        }
    }

    private String resolveCoverUrl(MultipartFile cover, FileUploadResponse storedVideo, Long currentUserId) {
        if (cover != null && !cover.isEmpty()) {
            return fileStorageService.store(cover, "cover", currentUserId).fileUrl();
        }
        Path videoPath = fileStorageService.resolve(storedVideo.fileKey());
        String generatedCoverUrl = videoCoverService.generateCoverUrl(videoPath, currentUserId);
        return generatedCoverUrl == null ? storedVideo.fileUrl() : generatedCoverUrl;
    }

    public Video getByVideoId(String videoId) {
        return videoRepository.findByVideoId(videoId).orElseThrow(() -> new BusinessException(404, "视频不存在"));
    }

    private CommentItemResponse toCommentItem(Comment comment) {
        User user = userRepository.findById(comment.getUserId()).orElseThrow(() -> new BusinessException(404, "用户不存在"));
        List<CommentItemResponse> replies = new ArrayList<>();
        for (Comment reply : commentRepository.findByParentIdAndStatusOrderByCreateTimeAsc(comment.getId(), 1)) {
            User replyUser = userRepository.findById(reply.getUserId()).orElseThrow(() -> new BusinessException(404, "用户不存在"));
            replies.add(VideoMapper.toCommentItem(reply, replyUser, List.of()));
        }
        return VideoMapper.toCommentItem(comment, user, replies);
    }

    private Long parseCommentId(String commentId) {
        String cleanId = commentId.startsWith("c_") ? commentId.substring(2) : commentId;
        return Long.parseLong(cleanId);
    }

    private void refreshAuthorLikeCount(Long authorId) {
        User user = userRepository.findById(authorId).orElseThrow(() -> new BusinessException(404, "用户不存在"));
        long totalLikes = videoRepository.findByAuthorIdOrderByCreateTimeDesc(authorId)
            .stream()
            .filter(video -> video.getStatus() == null || video.getStatus() == 1)
            .mapToLong(Video::getLikeCount)
            .sum();
        user.setLikeCount(totalLikes);
        userRepository.save(user);
    }
}
