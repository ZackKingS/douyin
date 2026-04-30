package com.douyin.backend.service;

import com.douyin.backend.common.BusinessException;
import com.douyin.backend.dto.user.FollowResponse;
import com.douyin.backend.dto.user.UpdateUserRequest;
import com.douyin.backend.dto.user.UserInfoDto;
import com.douyin.backend.dto.user.UserListResponse;
import com.douyin.backend.dto.user.UserProfileResponse;
import com.douyin.backend.dto.video.VideoListResponse;
import com.douyin.backend.dto.video.VideoMapper;
import com.douyin.backend.entity.Follow;
import com.douyin.backend.entity.User;
import com.douyin.backend.repository.FollowRepository;
import com.douyin.backend.repository.UserRepository;
import com.douyin.backend.repository.VideoLikeRepository;
import com.douyin.backend.repository.VideoRepository;
import com.douyin.backend.util.TimeUtils;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final VideoRepository videoRepository;
    private final VideoLikeRepository videoLikeRepository;

    public UserService(
        UserRepository userRepository,
        FollowRepository followRepository,
        VideoRepository videoRepository,
        VideoLikeRepository videoLikeRepository
    ) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.videoRepository = videoRepository;
        this.videoLikeRepository = videoLikeRepository;
    }

    public UserProfileResponse getProfile(Long userId, Long sourceUserId) {
        User user = getUser(userId);
        boolean isFollowing = sourceUserId != null && followRepository.existsByUserIdAndFollowId(sourceUserId, userId);
        boolean isFollowed = sourceUserId != null && followRepository.existsByUserIdAndFollowId(userId, sourceUserId);
        return new UserProfileResponse(
            user.getId(),
            user.getUsername(),
            user.getNickname(),
            user.getAvatar(),
            user.getGender(),
            user.getBirthday() == null ? null : user.getBirthday().toString(),
            user.getSignature(),
            user.getCountry(),
            user.getProvince(),
            user.getCity(),
            user.getFollowCount(),
            user.getFansCount(),
            user.getLikeCount(),
            user.getVideoCount(),
            isFollowing,
            isFollowed,
            isFollowing && isFollowed,
            calculateLevel(user),
            user.getVideoCount() > 3 ? "优质创作者" : null,
            TimeUtils.toIso(user.getCreateTime())
        );
    }

    public UserProfileResponse updateProfile(Long userId, UpdateUserRequest request) {
        User user = getUser(userId);
        if (request.getNickname() != null) user.setNickname(request.getNickname());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getBirthday() != null && !request.getBirthday().isBlank()) user.setBirthday(LocalDate.parse(request.getBirthday()));
        if (request.getSignature() != null) user.setSignature(request.getSignature());
        if (request.getCountry() != null) user.setCountry(request.getCountry());
        if (request.getProvince() != null) user.setProvince(request.getProvince());
        if (request.getCity() != null) user.setCity(request.getCity());
        userRepository.save(user);
        return getProfile(userId, userId);
    }

    public UserListResponse getFollows(Long userId, int page, int size) {
        List<Follow> follows = followRepository.findByUserIdOrderByCreateTimeDesc(userId);
        return toUserListResponse(follows.stream().skip((long) (page - 1) * size).limit(size).map(Follow::getFollowId).toList(), page, size, follows.size(), userId);
    }

    public UserListResponse getFans(Long userId, int page, int size) {
        List<Follow> fans = followRepository.findByFollowIdOrderByCreateTimeDesc(userId);
        return toUserListResponse(fans.stream().skip((long) (page - 1) * size).limit(size).map(Follow::getUserId).toList(), page, size, fans.size(), userId);
    }

    @Transactional
    public FollowResponse follow(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new BusinessException(400, "不能关注自己");
        }
        getUser(targetUserId);
        if (!followRepository.existsByUserIdAndFollowId(currentUserId, targetUserId)) {
            Follow follow = new Follow();
            follow.setUserId(currentUserId);
            follow.setFollowId(targetUserId);
            followRepository.save(follow);
            refreshFollowStats(currentUserId, targetUserId);
        }
        User self = getUser(currentUserId);
        User target = getUser(targetUserId);
        return new FollowResponse(true, self.getFollowCount(), target.getFansCount());
    }

    @Transactional
    public FollowResponse unfollow(Long currentUserId, Long targetUserId) {
        followRepository.findByUserIdAndFollowId(currentUserId, targetUserId).ifPresent(followRepository::delete);
        refreshFollowStats(currentUserId, targetUserId);
        User self = getUser(currentUserId);
        User target = getUser(targetUserId);
        return new FollowResponse(false, self.getFollowCount(), target.getFansCount());
    }

    public VideoListResponse getUserVideos(Long userId, int page, int size, Long sourceUserId) {
        List<com.douyin.backend.entity.Video> videos = videoRepository.findByAuthorIdAndStatusOrderByCreateTimeDesc(userId, 1, PageRequest.of(page - 1, size));
        int total = (int) videoRepository.countByAuthorIdAndStatus(userId, 1);
        return new VideoListResponse(
            videos.stream().map(video -> VideoMapper.toVideoItem(video, sourceUserId, userRepository, followRepository, videoLikeRepository)).toList(),
            page,
            size,
            total,
            page * size < total
        );
    }

    public UserInfoDto toUserInfo(User user, Long sourceUserId) {
        boolean isFollowing = sourceUserId != null && followRepository.existsByUserIdAndFollowId(sourceUserId, user.getId());
        boolean isMutual = isFollowing && followRepository.existsByUserIdAndFollowId(user.getId(), sourceUserId);
        return new UserInfoDto(
            user.getId(),
            user.getNickname(),
            user.getAvatar(),
            user.getSignature(),
            user.getFansCount(),
            user.getFollowCount(),
            isFollowing,
            isMutual,
            calculateLevel(user)
        );
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }

    private UserListResponse toUserListResponse(List<Long> userIds, int page, int size, int total, Long sourceUserId) {
        List<UserInfoDto> items = userIds.stream().map(this::getUser).map(user -> toUserInfo(user, sourceUserId)).toList();
        return new UserListResponse(items, page, size, total, page * size < total);
    }

    private void refreshFollowStats(Long userId, Long targetUserId) {
        User user = getUser(userId);
        User target = getUser(targetUserId);
        user.setFollowCount(followRepository.countByUserId(userId));
        target.setFansCount(followRepository.countByFollowId(targetUserId));
        userRepository.save(user);
        userRepository.save(target);
    }

    private int calculateLevel(User user) {
        return (int) Math.max(1, Math.min(10, user.getFansCount() / 100 + 1));
    }
}
