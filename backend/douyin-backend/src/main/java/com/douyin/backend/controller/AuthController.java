package com.douyin.backend.controller;

import com.douyin.backend.auth.CurrentUser;
import com.douyin.backend.auth.RequireLogin;
import com.douyin.backend.common.ApiResponse;
import com.douyin.backend.dto.auth.LoginRequest;
import com.douyin.backend.dto.auth.LoginResponse;
import com.douyin.backend.dto.auth.RefreshTokenRequest;
import com.douyin.backend.dto.auth.RegisterRequest;
import com.douyin.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success("注册成功", authService.register(request, httpRequest));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success("登录成功", authService.login(request, httpRequest));
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success("刷新成功", authService.refresh(request));
    }

    @RequireLogin
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@CurrentUser Long currentUserId) {
        authService.logout(currentUserId);
        return ApiResponse.success("退出成功", null);
    }
}
