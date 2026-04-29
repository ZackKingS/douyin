package com.douyin.backend.service;

import com.douyin.backend.common.BusinessException;
import com.douyin.backend.dto.auth.LoginRequest;
import com.douyin.backend.dto.auth.LoginResponse;
import com.douyin.backend.dto.auth.RefreshTokenRequest;
import com.douyin.backend.dto.auth.RegisterRequest;
import com.douyin.backend.entity.TokenSession;
import com.douyin.backend.entity.User;
import com.douyin.backend.repository.TokenSessionRepository;
import com.douyin.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final long EXPIRES_IN = 7 * 24 * 60 * 60L;

    private final UserRepository userRepository;
    private final TokenSessionRepository tokenSessionRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
        UserRepository userRepository,
        TokenSessionRepository tokenSessionRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.tokenSessionRepository = tokenSessionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        userRepository.findByUsername(request.getUsername()).ifPresent(user -> {
            throw new BusinessException(400, "用户名已存在");
        });
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            userRepository.findByPhone(request.getPhone()).ifPresent(user -> {
                throw new BusinessException(400, "手机号已存在");
            });
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(blankToNull(request.getPhone()));
        user.setEmail(blankToNull(request.getEmail()));
        user.setAvatar("");
        userRepository.save(user);
        return issueToken(user, httpRequest);
    }

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        User user = switch (request.getLoginType()) {
            case "password" -> loginWithPassword(request);
            case "phone", "sms" -> loginWithPhone(request);
            default -> throw new BusinessException(400, "不支持的登录方式");
        };
        return issueToken(user, httpRequest);
    }

    public LoginResponse refresh(RefreshTokenRequest request) {
        TokenSession session = tokenSessionRepository.findByRefreshTokenAndRevokedFalse(request.getRefreshToken())
            .orElseThrow(() -> new BusinessException(401, "refreshToken无效"));
        if (session.getExpiresAt() != null && session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setRevoked(true);
            tokenSessionRepository.save(session);
            throw new BusinessException(401, "refreshToken已过期");
        }
        User user = userRepository.findById(session.getUserId())
            .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        session.setRevoked(true);
        tokenSessionRepository.save(session);
        return issueToken(user, null);
    }

    @Transactional
    public void logout(Long userId) {
        tokenSessionRepository.findByUserIdAndRevokedFalse(userId).forEach(session -> session.setRevoked(true));
        tokenSessionRepository.flush();
    }

    private User loginWithPassword(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        return user;
    }

    private User loginWithPhone(LoginRequest request) {
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new BusinessException(400, "phone不能为空");
        }
        return userRepository.findByPhone(request.getPhone())
            .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }

    private LoginResponse issueToken(User user, HttpServletRequest httpRequest) {
        String accessToken = "atk_" + UUID.randomUUID().toString().replace("-", "");
        String refreshToken = "rtk_" + UUID.randomUUID().toString().replace("-", "");
        TokenSession session = new TokenSession();
        session.setUserId(user.getId());
        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(LocalDateTime.now().plusSeconds(EXPIRES_IN));
        tokenSessionRepository.save(session);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(httpRequest == null ? null : httpRequest.getRemoteAddr());
        userRepository.save(user);
        return new LoginResponse(
            user.getId(),
            user.getUsername(),
            user.getNickname(),
            user.getAvatar(),
            accessToken,
            refreshToken,
            EXPIRES_IN
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
