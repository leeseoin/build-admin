package com.BO.admin.service;

import com.BO.admin.dto.auth.LoginRequest;
import com.BO.admin.dto.auth.SignupRequest;
import com.BO.admin.dto.auth.TokenResponse;
import com.BO.admin.entity.User;
import com.BO.admin.repository.UserRepository;
import com.BO.admin.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 서비스
 * - 회원가입, 로그인, 로그아웃
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    /**
     * 회원가입
     * - 토큰 발급 없음 (로그인 시 발급)
     */
    @Transactional
    public User signup(SignupRequest request) {
        // 1. 중복 체크
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalArgumentException("이미 존재하는 loginId입니다: " + request.getLoginId());
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. subscribeInService 변환 (Boolean → String "Y"/"N")
        String subscribeValue = "N";
        if (request.getSubscribeInService() != null && request.getSubscribeInService()) {
            subscribeValue = "Y";
        }

        // 4. User 생성 및 저장
        User user = User.builder()
                .loginId(request.getLoginId())
                .password(encodedPassword)
                .userName(request.getUserName())
                .language(request.getLanguage() != null ? request.getLanguage() : "ko")
                .subscribeInService(subscribeValue)
                .build();

        User savedUser = userRepository.save(user);
        log.info("회원가입 성공: loginId={}, userSeq={}", savedUser.getLoginId(), savedUser.getUserSeq());

        return savedUser;
    }

    /**
     * 로그인
     * - Access Token, Refresh Token 발급
     */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        log.info("로그인 성공: loginId={}, userSeq={}", user.getLoginId(), user.getUserSeq());

        // 3. JWT 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getLoginId(),
                "USER",
                "USER"
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(
                user.getLoginId(),
                "USER"
        );

        // 4. Access Token을 User 테이블에 저장
        user.setAccessToken(accessToken);
        userRepository.save(user);

        // 5. Refresh Token을 Redis에 저장 (기존 토큰 덮어씀)
        refreshTokenService.saveRefreshToken(user.getLoginId(), refreshToken);

        // 6. 응답 반환
        return TokenResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenValidityMs(),
                "USER"
        );
    }

    /**
     * 로그아웃
     */
    public void logout(String loginId) {
        // Redis에서 Refresh Token 삭제
        refreshTokenService.deleteRefreshToken(loginId);
        log.info("로그아웃 완료: loginId={}", loginId);
    }
}
