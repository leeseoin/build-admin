package com.BO.admin.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * JwtTokenProvider 단위 테스트
 * - 토큰 생성 및 검증
 * - 토큰에서 정보 추출
 * - 토큰 만료 처리
 */
@DisplayName("JWT 토큰 Provider 테스트")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    // 테스트용 설정값
    private static final String TEST_SECRET = "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm-security-testing-purpose-only";
    private static final long ACCESS_TOKEN_VALIDITY = 3600000L; // 1시간
    private static final long REFRESH_TOKEN_VALIDITY = 604800000L; // 7일
    private static final long SHORT_VALIDITY = 1000L; // 1초 (만료 테스트용)

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                TEST_SECRET,
                ACCESS_TOKEN_VALIDITY,
                REFRESH_TOKEN_VALIDITY
        );
    }

    @Test
    @DisplayName("Access Token 생성 성공")
    void createAccessToken_Success() {
        // given
        String loginId = "testuser@example.com";
        String role = "USER";
        String userType = "USER";

        // when
        String token = jwtTokenProvider.createAccessToken(loginId, role, userType);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT는 3개 부분으로 구성
    }

    @Test
    @DisplayName("Refresh Token 생성 성공")
    void createRefreshToken_Success() {
        // given
        String loginId = "testuser@example.com";
        String userType = "USER";

        // when
        String token = jwtTokenProvider.createRefreshToken(loginId, userType);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("토큰에서 LoginId 추출 성공")
    void getLoginIdFromToken_Success() {
        // given
        String loginId = "testuser@example.com";
        String role = "USER";
        String userType = "USER";
        String token = jwtTokenProvider.createAccessToken(loginId, role, userType);

        // when
        String extractedLoginId = jwtTokenProvider.getLoginIdFromToken(token);

        // then
        assertThat(extractedLoginId).isEqualTo(loginId);
    }

    @Test
    @DisplayName("토큰에서 Role 추출 성공")
    void getRoleFromToken_Success() {
        // given
        String loginId = "admin@example.com";
        String role = "ADMIN";
        String userType = "ADMIN";
        String token = jwtTokenProvider.createAccessToken(loginId, role, userType);

        // when
        String extractedRole = jwtTokenProvider.getRoleFromToken(token);

        // then
        assertThat(extractedRole).isEqualTo(role);
    }

    @Test
    @DisplayName("토큰에서 UserType 추출 성공")
    void getUserTypeFromToken_Success() {
        // given
        String loginId = "testuser@example.com";
        String role = "USER";
        String userType = "USER";
        String token = jwtTokenProvider.createAccessToken(loginId, role, userType);

        // when
        String extractedUserType = jwtTokenProvider.getUserTypeFromToken(token);

        // then
        assertThat(extractedUserType).isEqualTo(userType);
    }

    @Test
    @DisplayName("토큰 타입 확인 - Access Token")
    void getTokenType_AccessToken() {
        // given
        String loginId = "testuser@example.com";
        String role = "USER";
        String userType = "USER";
        String token = jwtTokenProvider.createAccessToken(loginId, role, userType);

        // when
        String tokenType = jwtTokenProvider.getTokenType(token);

        // then
        assertThat(tokenType).isEqualTo("access");
    }

    @Test
    @DisplayName("토큰 타입 확인 - Refresh Token")
    void getTokenType_RefreshToken() {
        // given
        String loginId = "testuser@example.com";
        String userType = "USER";
        String token = jwtTokenProvider.createRefreshToken(loginId, userType);

        // when
        String tokenType = jwtTokenProvider.getTokenType(token);

        // then
        assertThat(tokenType).isEqualTo("refresh");
    }

    @Test
    @DisplayName("유효한 토큰 검증 성공")
    void validateToken_ValidToken_ReturnsTrue() {
        // given
        String loginId = "testuser@example.com";
        String role = "USER";
        String userType = "USER";
        String token = jwtTokenProvider.createAccessToken(loginId, role, userType);

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("잘못된 형식의 토큰 검증 실패")
    void validateToken_MalformedToken_ReturnsFalse() {
        // given
        String malformedToken = "this.is.not.a.valid.jwt.token";

        // when
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("빈 토큰 검증 실패")
    void validateToken_EmptyToken_ReturnsFalse() {
        // given
        String emptyToken = "";

        // when
        boolean isValid = jwtTokenProvider.validateToken(emptyToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void validateToken_ExpiredToken_ReturnsFalse() throws InterruptedException {
        // given: 1초 후 만료되는 토큰을 가진 Provider 생성
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(
                TEST_SECRET,
                SHORT_VALIDITY, // 1초
                REFRESH_TOKEN_VALIDITY
        );
        String token = shortLivedProvider.createAccessToken("test@example.com", "USER", "USER");

        // when: 2초 대기 (토큰 만료)
        Thread.sleep(2000);
        boolean isValid = shortLivedProvider.validateToken(token);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰 확인 - isTokenExpired")
    void isTokenExpired_ExpiredToken_ReturnsTrue() throws InterruptedException {
        // given: 1초 후 만료되는 토큰을 가진 Provider 생성
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(
                TEST_SECRET,
                SHORT_VALIDITY, // 1초
                REFRESH_TOKEN_VALIDITY
        );
        String token = shortLivedProvider.createAccessToken("test@example.com", "USER", "USER");

        // when: 2초 대기 (토큰 만료)
        Thread.sleep(2000);
        boolean isExpired = shortLivedProvider.isTokenExpired(token);

        // then
        assertThat(isExpired).isTrue();
    }

    @Test
    @DisplayName("유효한 토큰 만료 확인 - isTokenExpired")
    void isTokenExpired_ValidToken_ReturnsFalse() {
        // given
        String token = jwtTokenProvider.createAccessToken("test@example.com", "USER", "USER");

        // when
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        // then
        assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("Access Token 만료 시간 확인")
    void getAccessTokenValidityMs_ReturnsCorrectValue() {
        // when
        long validity = jwtTokenProvider.getAccessTokenValidityMs();

        // then
        assertThat(validity).isEqualTo(ACCESS_TOKEN_VALIDITY);
    }

    @Test
    @DisplayName("Refresh Token 만료 시간 확인")
    void getRefreshTokenValidityMs_ReturnsCorrectValue() {
        // when
        long validity = jwtTokenProvider.getRefreshTokenValidityMs();

        // then
        assertThat(validity).isEqualTo(REFRESH_TOKEN_VALIDITY);
    }

    @Test
    @DisplayName("ADMIN 사용자 토큰 생성 및 검증")
    void createAdminToken_Success() {
        // given
        String loginId = "admin@example.com";
        String role = "SUPER_ADMIN";
        String userType = "ADMIN";

        // when
        String token = jwtTokenProvider.createAccessToken(loginId, role, userType);

        // then
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getLoginIdFromToken(token)).isEqualTo(loginId);
        assertThat(jwtTokenProvider.getRoleFromToken(token)).isEqualTo(role);
        assertThat(jwtTokenProvider.getUserTypeFromToken(token)).isEqualTo(userType);
    }

    @Test
    @DisplayName("여러 사용자 타입의 토큰 생성 및 구분")
    void createTokens_DifferentUserTypes_CanDistinguish() {
        // given
        String userToken = jwtTokenProvider.createAccessToken("user@example.com", "USER", "USER");
        String adminToken = jwtTokenProvider.createAccessToken("admin@example.com", "ADMIN", "ADMIN");

        // when
        String userType1 = jwtTokenProvider.getUserTypeFromToken(userToken);
        String userType2 = jwtTokenProvider.getUserTypeFromToken(adminToken);

        // then
        assertThat(userType1).isEqualTo("USER");
        assertThat(userType2).isEqualTo("ADMIN");
    }
}
