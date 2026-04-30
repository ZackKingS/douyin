package com.douyin.backend.repository;

import com.douyin.backend.entity.VideoLike;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoLikeRepository extends JpaRepository<VideoLike, Long> {
    Optional<VideoLike> findByUserIdAndVideoId(Long userId, Long videoId);
    boolean existsByUserIdAndVideoId(Long userId, Long videoId);
    void deleteByVideoId(Long videoId);
}
