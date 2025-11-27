package com.BO.admin.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JWT 토큰 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {

    /**
     * Access Token
     */
    private String accessToken;

    /**
     * Refresh Token
     */
    private String refreshToken;

    /**
     * 토큰 타입 (Bearer)
     */
    private String tokenType;

    /**
     * Access Token 만료 시간 (ms)
     */
    private Long expiresIn;

    /**
     * 사용자 타입 (USER, ADMIN)
     */
    private String userType;

    /**
     * 정적 팩토리 메서드
     */
    public static TokenResponse of(String accessToken, String refreshToken, Long expiresIn, String userType) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .userType(userType)
                .build();
    }
}
