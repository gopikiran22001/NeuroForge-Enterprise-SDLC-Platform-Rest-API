package com.stdace.neuroforge.service.auth;

import com.stdace.neuroforge.dto.auth.AuthResponse;
import com.stdace.neuroforge.dto.auth.LoginRequest;
import com.stdace.neuroforge.dto.auth.RefreshTokenRequest;
import com.stdace.neuroforge.dto.auth.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    // New method to refresh token from HTTP request (extracts from cookies)
    AuthResponse refreshToken(HttpServletRequest request);

    void logout(HttpServletResponse response);
}



