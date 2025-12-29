package com.BO.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Refresh Token 관리 서비스 (Redis 기반)
 *
 * Redis Key 구조:
 * - refresh:{loginId} → refreshToken 값
 * - TTL: 7일 (jwt.refresh-token-validity)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidityMs;

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    /**
     * Refresh Token 저장
     */
    public void saveRefreshToken(String loginId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + loginId;
        long ttlSeconds = refreshTokenValidityMs / 1000;

        redisTemplate.opsForValue().set(key, refreshToken, ttlSeconds, TimeUnit.SECONDS);
        log.info("Refresh Token 저장: loginId={}, TTL={}초", loginId, ttlSeconds);
    }

    /**
     * Refresh Token 조회
     */
    public String getRefreshToken(String loginId) {
        String key = REFRESH_TOKEN_PREFIX + loginId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Refresh Token 삭제 (로그아웃 시 사용)
     */
    public void deleteRefreshToken(String loginId) {
        String key = REFRESH_TOKEN_PREFIX + loginId;
        Boolean deleted = redisTemplate.delete(key);
        log.info("Refresh Token 삭제: loginId={}, 결과={}", loginId, deleted);
    }

    /**
     * Refresh Token 유효성 확인
     * - Redis에 저장된 토큰과 요청 토큰이 일치하는지 확인
     */
    public boolean validateRefreshToken(String loginId, String refreshToken) {
        String storedToken = getRefreshToken(loginId);
        if (storedToken == null) {
            log.warn("저장된 Refresh Token 없음: loginId={}", loginId);
            return false;
        }
        return storedToken.equals(refreshToken);
    }
}
