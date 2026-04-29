package com.douyin.backend.repository;

import com.douyin.backend.entity.TokenSession;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenSessionRepository extends JpaRepository<TokenSession, Long> {
    Optional<TokenSession> findByAccessTokenAndRevokedFalse(String accessToken);
    Optional<TokenSession> findByRefreshTokenAndRevokedFalse(String refreshToken);
    List<TokenSession> findByUserIdAndRevokedFalse(Long userId);
}
