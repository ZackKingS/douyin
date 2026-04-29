package com.douyin.backend.service;

import com.douyin.backend.dto.search.*;
import com.douyin.backend.dto.user.UserInfoDto;
import com.douyin.backend.dto.video.TopicInfoDto;
import com.douyin.backend.dto.video.VideoItemResponse;
import com.douyin.backend.dto.video.VideoMapper;
import com.douyin.backend.entity.User;
import com.douyin.backend.entity.Video;
import com.douyin.backend.repository.FollowRepository;
import com.douyin.backend.repository.UserRepository;
import com.douyin.backend.repository.VideoLikeRepository;
import com.douyin.backend.repository.VideoRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final FollowRepository followRepository;
    private final VideoLikeRepository videoLikeRepository;

    public SearchService(
        UserRepository userRepository,
        VideoRepository videoRepository,
        FollowRepository followRepository,
        VideoLikeRepository videoLikeRepository
    ) {
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
        this.followRepository = followRepository;
        this.videoLikeRepository = videoLikeRepository;
    }

    public SearchResponse search(String keyword, int page, int size, Long currentUserId) {
        List<UserInfoDto> users = userRepository.findAll().stream()
            .filter(user -> containsIgnoreCase(user.getNickname(), keyword) || containsIgnoreCase(user.getUsername(), keyword))
            .limit(size)
            .map(user -> toUserInfo(user, currentUserId))
            .toList();
        List<VideoItemResponse> videos = videoRepository
            .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCreateTimeDesc(keyword, keyword, PageRequest.of(page - 1, size))
            .stream()
            .map(video -> VideoMapper.toVideoItem(video, currentUserId, userRepository, followRepository, videoLikeRepository))
            .toList();
        List<TopicInfoDto> topics = List.of(
            new TopicInfoDto(1L, keyword),
            new TopicInfoDto(2L, keyword + "日常")
        );
        return new SearchResponse(
            keyword,
            new SearchUsers(users, users.size()),
            new SearchVideos(videos, videos.size()),
            new SearchTopics(topics, topics.size()),
            page,
            size,
            videos.size() == size
        );
    }

    public SearchHotResponse hot() {
        return new SearchHotResponse(
            List.of(
                new HotSearchItem(1, "搞笑", 980_000, "up", 450_000),
                new HotSearchItem(2, "探店", 860_000, "steady", 390_000),
                new HotSearchItem(3, "旅行vlog", 740_000, "up", 320_000)
            ),
            OffsetDateTime.now().toString()
        );
    }

    private UserInfoDto toUserInfo(User user, Long currentUserId) {
        boolean isFollowing = currentUserId != null && followRepository.existsByUserIdAndFollowId(currentUserId, user.getId());
        boolean isMutual = isFollowing && currentUserId != null && followRepository.existsByUserIdAndFollowId(user.getId(), currentUserId);
        return new UserInfoDto(user.getId(), user.getNickname(), user.getAvatar(), user.getSignature(), user.getFansCount(), user.getFollowCount(), isFollowing, isMutual, 1);
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return source != null && keyword != null && source.toLowerCase().contains(keyword.toLowerCase());
    }
}
