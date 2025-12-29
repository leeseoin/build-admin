package com.BO.admin.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.BO.admin.dto.auth.LoginRequest;
import com.BO.admin.dto.auth.SignupRequest;
import com.BO.admin.dto.auth.TokenResponse;
import com.BO.admin.entity.User;
import com.BO.admin.security.jwt.JwtTokenProvider;
import com.BO.admin.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 API (회원가입, 로그인, 로그아웃)")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다. (토큰 발급 없음, 로그인할 때 accessToken, refreshtoken 토큰 발급함)")
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            log.info("회원가입 요청: loginId={}", request.getLoginId());
            User savedUser = authService.signup(request);

            // 회원가입 성공 응답 (토큰 없음)
            Map<String, Object> response = new HashMap<>();
            response.put("message", "회원가입이 완료되었습니다. 로그인해주세요.");
            response.put("loginId", savedUser.getLoginId());
            response.put("userName", savedUser.getUserName());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("회원가입 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("회원가입 처리 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "로그인", description = "로그인 후 JWT 토큰을 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            log.info("로그인 요청: loginId={}", request.getLoginId());
            TokenResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("로그인 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("로그인 처리 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "로그아웃", description = "로그아웃하고 Refresh Token을 삭제합니다.")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            // Bearer 토큰에서 JWT 추출
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Authorization 헤더가 필요합니다."));
            }

            String token = authHeader.substring(7);

            // 토큰에서 loginId 추출
            String loginId = jwtTokenProvider.getLoginIdFromToken(token);

            // 로그아웃 처리 (Redis에서 Refresh Token 삭제)
            authService.logout(loginId);

            log.info("로그아웃 성공: loginId={}", loginId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "로그아웃 되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("로그아웃 처리 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "헬스 체크", description = "서버 상태를 확인합니다.")
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Admin Auth API");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * 에러 응답 생성
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
