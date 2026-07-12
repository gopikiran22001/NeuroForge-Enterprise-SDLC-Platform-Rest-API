package com.stdace.neuroforge.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    @Value("${security.jwt.cookie.access-token-name:accessToken}")
    private String accessTokenCookieName;

    @Value("${security.jwt.cookie.refresh-token-name:refreshToken}")
    private String refreshTokenCookieName;

    @Value("${security.jwt.cookie.http-only:true}")
    private boolean httpOnly;

    @Value("${security.jwt.cookie.secure:true}")
    private boolean secure;

    @Value("${security.jwt.cookie.same-site:Strict}")
    private String sameSite;

    @Value("${security.jwt.cookie.path:/}")
    private String path;

    @Value("${security.jwt.access-token-expiration-seconds:3600}")
    private int accessTokenExpiry;

    @Value("${security.jwt.refresh-token-expiration-seconds:604800}")
    private int refreshTokenExpiry;

    /**
     * Creates and sets an access token cookie in the response
     */
    public void setAccessTokenCookie(HttpServletResponse response, String token) {
        setCookie(response, accessTokenCookieName, token, accessTokenExpiry);
    }

    /**
     * Creates and sets a refresh token cookie in the response
     */
    public void setRefreshTokenCookie(HttpServletResponse response, String token) {
        setCookie(response, refreshTokenCookieName, token, refreshTokenExpiry);
    }

    /**
     * Creates and sets a cookie with secure attributes
     */
    private void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", sameSite);
        response.addCookie(cookie);
    }

    /**
     * Retrieves the access token from cookies
     */
    public Optional<String> getAccessTokenFromCookies(HttpServletRequest request) {
        return getTokenFromCookies(request, accessTokenCookieName);
    }

    /**
     * Retrieves the refresh token from cookies
     */
    public Optional<String> getRefreshTokenFromCookies(HttpServletRequest request) {
        return getTokenFromCookies(request, refreshTokenCookieName);
    }

    /**
     * Retrieves a specific token from cookies by name
     */
    private Optional<String> getTokenFromCookies(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue()))
                .map(Cookie::getValue)
                .findFirst();
    }

    /**
     * Clears both access and refresh token cookies
     */
    public void clearAuthenticationCookies(HttpServletResponse response) {
        clearCookie(response, accessTokenCookieName);
        clearCookie(response, refreshTokenCookieName);
    }

    /**
     * Clears a specific cookie
     */
    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setPath(path);
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", sameSite);
        response.addCookie(cookie);
    }
}

