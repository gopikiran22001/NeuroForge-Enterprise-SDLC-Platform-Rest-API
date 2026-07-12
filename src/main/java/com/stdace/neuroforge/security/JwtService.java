package com.stdace.neuroforge.security;

import com.stdace.neuroforge.models.RefreshToken;
import com.stdace.neuroforge.models.User;
import com.stdace.neuroforge.config.JwtProperties;
import com.stdace.neuroforge.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    private final RefreshTokenRepository refreshTokenRepository;


    public String generateAccessToken(User user) {
        return buildToken(user, jwtProperties.getAccessTokenExpirationSeconds(), "ACCESS");
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to hash refresh token", e);
        }
    }

    public String generateRefreshToken(User user) {
        String token =  buildToken(user, jwtProperties.getRefreshTokenExpirationSeconds(), "REFRESH");

        RefreshToken refreshToken = refreshTokenRepository
                .findByUserId(user.getId())
                .orElse(new RefreshToken());

        refreshToken.setUser(user);
        refreshToken.setToken(hashToken(token));

        refreshTokenRepository.save(refreshToken);

        return token;
    }

    public String extractUsername(String token) {
        return parseClaims(token).get("sub");
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        if(isRefreshToken(token) && !refreshTokenRepository.findByToken(hashToken(token))) {
            return false;
        }
        return extractUsername(token).equalsIgnoreCase(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isRefreshToken(String token) {
        return "REFRESH".equals(parseClaims(token).get("tokenType"));
    }

    public UUID extractUserId(String token) {
        String userId = parseClaims(token).get("userId");
        return userId == null ? null : UUID.fromString(userId);
    }

    private String buildToken(User user, long expirationSeconds, String tokenType) {
        Map<String, String> claims = new LinkedHashMap<>();
        claims.put("userId", user.getId() != null ? user.getId().toString() : null);
        claims.put("role", user.getRole().name());
        claims.put("tokenType", tokenType);
        Instant now = Instant.now();
        String payload = String.join("|",
                user.getEmail(),
                Long.toString(now.getEpochSecond()),
                Long.toString(now.plusSeconds(expirationSeconds).getEpochSecond()),
                claims.get("userId"),
                claims.get("role"),
                claims.get("tokenType")
        );
        return sign(payload) + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    private boolean isTokenExpired(String token) {
        return Instant.ofEpochSecond(Long.parseLong(parseClaims(token).get("exp"))).isBefore(Instant.now());
    }

    private Map<String, String> parseClaims(String token) {
        String[] parts = splitToken(token);
        String signature = parts[0];
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        if (!signature.equals(sign(payload))) {
            throw new IllegalArgumentException("Invalid token signature");
        }
        String[] values = payload.split("\\|", -1);
        Map<String, String> claims = new LinkedHashMap<>();
        claims.put("sub", values[0]);
        claims.put("iat", values[1]);
        claims.put("exp", values[2]);
        claims.put("userId", values[3].isBlank() ? null : values[3]);
        claims.put("role", values[4]);
        claims.put("tokenType", values[5]);
        return claims;
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes(), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign token", ex);
        }
    }

    private String[] splitToken(String token) {
        String[] parts = token.split("\\.", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid token format");
        }
        return parts;
    }

    private byte[] secretBytes() {
        String secret = jwtProperties.getSecret();
        try {
            return Base64.getUrlDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }
}
