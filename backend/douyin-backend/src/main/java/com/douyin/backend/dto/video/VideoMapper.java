package com.douyin.backend.dto.video;

import com.douyin.backend.dto.user.UserInfoDto;
import com.douyin.backend.entity.Comment;
import com.douyin.backend.entity.User;
import com.douyin.backend.entity.Video;
import com.douyin.backend.repository.FollowRepository;
import com.douyin.backend.repository.UserRepository;
import com.douyin.backend.repository.VideoLikeRepository;
import com.douyin.backend.util.TimeUtils;
import java.util.List;

public final class VideoMapper {

    private VideoMapper() {
    }

    public static VideoItemResponse toVideoItem(
        Video video,
        Long currentUserId,
        UserRepository userRepository,
        FollowRepository followRepository,
        VideoLikeRepository videoLikeRepository
    ) {
        User author = userRepository.findById(video.getAuthorId()).orElse(null);
        boolean isLiked = currentUserId != null && videoLikeRepository.existsByUserIdAndVideoId(currentUserId, video.getId());
        UserInfoDto authorDto = author == null ? null : toUserInfo(author, currentUserId, followRepository);
        return new VideoItemResponse(
            video.getVideoId(),
            authorDto,
            video.getTitle(),
            video.getDescription(),
            video.getVideoUrl(),
            video.getCoverUrl(),
            video.getDuration(),
            video.getWidth(),
            video.getHeight(),
            video.getLikeCount(),
            video.getCommentCount(),
            video.getShareCount(),
            video.getCollectCount(),
            video.getViewCount(),
            isLiked,
            false,
            firstTopicName(video.getTopicIds()),
            toMusic(video),
            video.getLocation(),
            TimeUtils.toIso(video.getCreateTime()),
            video.getCreateTime() == null ? null : video.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        );
    }

    public static VideoDetailResponse toVideoDetail(
        Video video,
        Long currentUserId,
        UserRepository userRepository,
        FollowRepository followRepository,
        VideoLikeRepository videoLikeRepository
    ) {
        User author = userRepository.findById(video.getAuthorId()).orElse(null);
        boolean isLiked = currentUserId != null && videoLikeRepository.existsByUserIdAndVideoId(currentUserId, video.getId());
        boolean isFollowing = currentUserId != null && followRepository.existsByUserIdAndFollowId(currentUserId, video.getAuthorId());
        return new VideoDetailResponse(
            video.getVideoId(),
            author == null ? null : toUserInfo(author, currentUserId, followRepository),
            video.getTitle(),
            video.getDescription(),
            video.getVideoUrl(),
            video.getCoverUrl(),
            video.getDuration(),
            video.getWidth(),
            video.getHeight(),
            video.getFileSize(),
            video.getLikeCount(),
            video.getCommentCount(),
            video.getShareCount(),
            video.getCollectCount(),
            video.getViewCount(),
            isLiked,
            false,
            isFollowing,
            toTopics(video.getTopicIds()),
            toMusic(video),
            video.getLocation(),
            video.getLatitude() == null ? null : video.getLatitude().doubleValue(),
            video.getLongitude() == null ? null : video.getLongitude().doubleValue(),
            TimeUtils.toIso(video.getCreateTime())
        );
    }

    public static CommentItemResponse toCommentItem(Comment comment, User user, List<CommentItemResponse> replies) {
        return new CommentItemResponse(
            "c_" + comment.getId(),
            new UserInfoDto(user.getId(), user.getNickname(), user.getAvatar(), user.getSignature(), user.getFansCount(), user.getFollowCount(), false, false, 1),
            comment.getContent(),
            comment.getLikeCount(),
            comment.getReplyCount(),
            false,
            TimeUtils.toIso(comment.getCreateTime()),
            replies
        );
    }

    private static UserInfoDto toUserInfo(User user, Long currentUserId, FollowRepository followRepository) {
        boolean isFollowing = currentUserId != null && followRepository.existsByUserIdAndFollowId(currentUserId, user.getId());
        boolean isMutual = isFollowing && currentUserId != null && followRepository.existsByUserIdAndFollowId(user.getId(), currentUserId);
        return new UserInfoDto(
            user.getId(),
            user.getNickname(),
            user.getAvatar(),
            user.getSignature(),
            user.getFansCount(),
            user.getFollowCount(),
            isFollowing,
            isMutual,
            (int) Math.max(1, Math.min(10, user.getFansCount() / 100 + 1))
        );
    }

    private static List<TopicInfoDto> toTopics(String topicIds) {
        if (topicIds == null || topicIds.isBlank()) {
            return List.of();
        }
        String[] parts = topicIds.split(",");
        return java.util.stream.IntStream.range(0, parts.length)
            .mapToObj(index -> new TopicInfoDto((long) (index + 1), "#" + parts[index].trim()))
            .toList();
    }

    private static String firstTopicName(String topicIds) {
        if (topicIds == null || topicIds.isBlank()) {
            return null;
        }
        String first = topicIds.split(",")[0].trim();
        return "#" + first;
    }

    private static MusicInfoDto toMusic(Video video) {
        if (video.getMusicId() == null || video.getMusicId().isBlank()) {
            return null;
        }
        return new MusicInfoDto(video.getMusicId(), "热门BGM", "平台音乐库", null);
    }
}
