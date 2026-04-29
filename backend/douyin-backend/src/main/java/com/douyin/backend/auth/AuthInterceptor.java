package com.douyin.backend.auth;

import com.douyin.backend.common.BusinessException;
import com.douyin.backend.entity.TokenSession;
import com.douyin.backend.repository.TokenSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenSessionRepository tokenSessionRepository;

    public AuthInterceptor(TokenSessionRepository tokenSessionRepository) {
        this.tokenSessionRepository = tokenSessionRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            Optional<TokenSession> session = tokenSessionRepository.findByAccessTokenAndRevokedFalse(token);
            session.ifPresent(value -> AuthContext.setUserId(value.getUserId()));
        }

        boolean loginRequired = handlerMethod.hasMethodAnnotation(RequireLogin.class)
            || handlerMethod.getBeanType().isAnnotationPresent(RequireLogin.class);
        if (loginRequired && AuthContext.getUserId() == null) {
            throw new BusinessException(401, "未授权 / Token失效");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }
}
