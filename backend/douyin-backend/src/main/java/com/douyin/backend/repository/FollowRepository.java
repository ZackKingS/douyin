package com.douyin.backend.repository;

import com.douyin.backend.entity.Follow;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByUserIdAndFollowId(Long userId, Long followId);
    List<Follow> findByUserIdOrderByCreateTimeDesc(Long userId);
    List<Follow> findByFollowIdOrderByCreateTimeDesc(Long followId);
    long countByUserId(Long userId);
    long countByFollowId(Long followId);
    boolean existsByUserIdAndFollowId(Long userId, Long followId);
}
