package com.stdace.neuroforge.controller;

import com.stdace.neuroforge.common.ApiResponse;
import com.stdace.neuroforge.dto.auth.AuthResponse;
import com.stdace.neuroforge.dto.auth.LoginRequest;
import com.stdace.neuroforge.dto.auth.RefreshTokenRequest;
import com.stdace.neuroforge.dto.auth.RegisterRequest;
import com.stdace.neuroforge.service.auth.AuthService;
import com.stdace.neuroforge.security.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request);
        // Set tokens as secure HTTP-only cookies
        cookieUtil.setAccessTokenCookie(response, authResponse.getAccessToken());
        cookieUtil.setRefreshTokenCookie(response, authResponse.getRefreshToken());
        // Return response without tokens in body (tokens are now in cookies)
        authResponse.setAccessToken(null);
        authResponse.setRefreshToken(null);
        return ResponseEntity.ok(ApiResponse.success("Registered successfully", authResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        // Set tokens as secure HTTP-only cookies
        cookieUtil.setAccessTokenCookie(response, authResponse.getAccessToken());
        cookieUtil.setRefreshTokenCookie(response, authResponse.getRefreshToken());
        // Return response without tokens in body (tokens are now in cookies)
        authResponse.setAccessToken(null);
        authResponse.setRefreshToken(null);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.refreshToken(request);
        // Set new tokens as secure HTTP-only cookies
        cookieUtil.setAccessTokenCookie(response, authResponse.getAccessToken());
        cookieUtil.setRefreshTokenCookie(response, authResponse.getRefreshToken());
        // Return response without tokens in body (tokens are now in cookies)
        authResponse.setAccessToken(null);
        authResponse.setRefreshToken(null);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        // Clear authentication cookies
        authService.logout(response);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}



