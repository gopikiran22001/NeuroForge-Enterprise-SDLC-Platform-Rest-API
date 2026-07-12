package com.stdace.neuroforge.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    // Token is now retrieved from secure HTTP-only cookie by JwtAuthenticationFilter
    // This field is kept for backward compatibility but is optional
    private String refreshToken;
}



