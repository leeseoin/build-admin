package com.BO.admin.config;

import com.BO.admin.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SecurityConfig 통합 테스트
 * - URL별 권한 검증
 * - JWT 인증 필터 동작 확인
 * - CORS 헤더 확인
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Spring Security 설정 통합 테스트")
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // ========================================
    // URL별 권한 테스트
    // ========================================

    @Test
    @DisplayName("/api/auth/** - 인증 없이 접근 가능")
    void authEndpoint_NoAuthRequired() throws Exception {
        mockMvc.perform(get("/api/auth/test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // 404 = 엔드포인트는 없지만 Security는 통과
    }

    @Test
    @DisplayName("/api/public/** - 인증 없이 접근 가능")
    void publicEndpoint_NoAuthRequired() throws Exception {
        mockMvc.perform(get("/api/public/test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // 404 = 엔드포인트는 없지만 Security는 통과
    }

    @Test
    @DisplayName("/actuator/health - 인증 없이 접근 가능 (Security는 통과)")
    void healthEndpoint_NoAuthRequired() throws Exception {
        mockMvc.perform(get("/actuator/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Security 통과, Actuator 없으면 404
    }

    @Test
    @DisplayName("/api/user/** - 인증 없이 접근 시 401 Unauthorized")
    void userEndpoint_NoAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/api/admin/** - 인증 없이 접근 시 401 Unauthorized")
    void adminEndpoint_NoAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/api/super-admin/** - 인증 없이 접근 시 401 Unauthorized")
    void superAdminEndpoint_NoAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/super-admin/settings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ========================================
    // JWT 인증 테스트
    // ========================================

    @Test
    @DisplayName("USER 권한으로 /api/user/** 접근 가능")
    void userEndpoint_WithUserToken_Returns404() throws Exception {
        // given: USER 토큰 생성
        String token = jwtTokenProvider.createAccessToken(
                "testuser@example.com",
                "USER",
                "USER"
        );

        // when & then: /api/user/** 엔드포인트에 접근 (실제 컨트롤러 없으면 404)
        mockMvc.perform(get("/api/user/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // 404 = Security 통과, 컨트롤러 없음
    }

    @Test
    @DisplayName("ADMIN 권한으로 /api/user/** 접근 가능")
    void userEndpoint_WithAdminToken_Returns404() throws Exception {
        // given: ADMIN 토큰 생성
        String token = jwtTokenProvider.createAccessToken(
                "admin@example.com",
                "ADMIN",
                "ADMIN"
        );

        // when & then
        mockMvc.perform(get("/api/user/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // ADMIN도 /api/user/** 접근 가능
    }

    @Test
    @DisplayName("USER 권한으로 /api/admin/** 접근 시 403 Forbidden")
    void adminEndpoint_WithUserToken_Returns403() throws Exception {
        // given: USER 토큰 생성
        String token = jwtTokenProvider.createAccessToken(
                "testuser@example.com",
                "USER",
                "USER"
        );

        // when & then: USER는 /api/admin/** 접근 불가
        mockMvc.perform(get("/api/admin/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN 권한으로 /api/admin/** 접근 가능")
    void adminEndpoint_WithAdminToken_Returns404() throws Exception {
        // given: ADMIN 토큰 생성
        String token = jwtTokenProvider.createAccessToken(
                "admin@example.com",
                "ADMIN",
                "ADMIN"
        );

        // when & then
        mockMvc.perform(get("/api/admin/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Security 통과, 컨트롤러 없음
    }

    @Test
    @DisplayName("ADMIN 권한으로 /api/super-admin/** 접근 시 403 Forbidden")
    void superAdminEndpoint_WithAdminToken_Returns403() throws Exception {
        // given: ADMIN 토큰 생성
        String token = jwtTokenProvider.createAccessToken(
                "admin@example.com",
                "ADMIN",
                "ADMIN"
        );

        // when & then: ADMIN은 SUPER_ADMIN 엔드포인트 접근 불가
        mockMvc.perform(get("/api/super-admin/settings")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SUPER_ADMIN 권한으로 /api/super-admin/** 접근 가능")
    void superAdminEndpoint_WithSuperAdminToken_Returns404() throws Exception {
        // given: SUPER_ADMIN 토큰 생성
        String token = jwtTokenProvider.createAccessToken(
                "superadmin@example.com",
                "SUPER_ADMIN",
                "ADMIN"
        );

        // when & then
        mockMvc.perform(get("/api/super-admin/settings")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Security 통과, 컨트롤러 없음
    }

    @Test
    @DisplayName("잘못된 형식의 토큰으로 접근 시 401 Unauthorized")
    void invalidToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/user/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.here")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Bearer 없는 토큰으로 접근 시 401 Unauthorized")
    void tokenWithoutBearer_Returns401() throws Exception {
        String token = jwtTokenProvider.createAccessToken(
                "testuser@example.com",
                "USER",
                "USER"
        );

        mockMvc.perform(get("/api/user/profile")
                        .header(HttpHeaders.AUTHORIZATION, token) // Bearer 누락
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ========================================
    // CORS 테스트
    // ========================================

    @Test
    @DisplayName("CORS Preflight 요청 - localhost:3000 허용")
    void corsPreflight_Localhost3000_Allowed() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    @DisplayName("CORS Preflight 요청 - localhost:5173 허용")
    void corsPreflight_Localhost5173_Allowed() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    @DisplayName("CORS - 허용된 메서드 확인 (GET, POST, PUT, DELETE, PATCH)")
    void corsAllowedMethods_Check() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
    }

    @Test
    @DisplayName("CORS - Authorization 헤더 노출 확인")
    void corsExposedHeaders_Authorization() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization"));
    }

    // ========================================
    // 기타 Security 기능 테스트
    // ========================================

    @Test
    @DisplayName("CSRF 토큰 없이 POST 요청 가능 (CSRF 비활성화)")
    void csrfDisabled_PostWithoutToken_Allowed() throws Exception {
        // CSRF가 비활성화되어 있으므로 CSRF 토큰 없이도 POST 가능
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isNotFound()); // 404 = Security 통과, 컨트롤러 없음
    }

    @Test
    @DisplayName("세션이 생성되지 않음 (STATELESS)")
    void sessionStateless_NoSessionCreated() throws Exception {
        // given: 인증된 요청
        String token = jwtTokenProvider.createAccessToken(
                "testuser@example.com",
                "USER",
                "USER"
        );

        // when & then: 세션이 생성되지 않아야 함
        mockMvc.perform(get("/api/user/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(request().sessionAttribute("SPRING_SECURITY_CONTEXT", org.hamcrest.Matchers.nullValue()));
    }
}
