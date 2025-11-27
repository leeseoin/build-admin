# 구현 TODO 리스트

## 구현 순서 전략

복잡한 기능을 단계별로 나눠서 구현합니다. 각 단계마다 테스트를 하면서 진행합니다.

---

## Phase 1: 기본 설정 및 준비 (Foundation)

### 1.1 Dependencies 추가
- [ ] `build.gradle`에 필요한 라이브러리 추가
  - Spring Boot Starter (Web, Security, JPA, OAuth2 Client, Validation)
  - JWT 라이브러리 (jjwt)
  - MySQL Driver
  - Lombok

### 1.2 데이터베이스 설정
- [ ] `application.properties` 또는 `application.yml` 설정
  - 데이터베이스 연결 정보
  - JPA 설정
  - JWT 설정 (secret, expiration)
  - OAuth2 설정 (Google, Kakao client ID/secret)

### 1.3 데이터베이스 테이블 생성
- [ ] MySQL에서 `admin_db` 데이터베이스 생성
- [ ] `tb_refresh_token` 테이블 추가 (기존 SQL에 없음)
```sql
CREATE TABLE `tb_refresh_token` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `token` VARCHAR(500) NOT NULL UNIQUE,
  `userType` VARCHAR(10) NOT NULL COMMENT 'USER or ADMIN',
  `userId` BIGINT UNSIGNED NOT NULL,
  `expiryDate` DATETIME NOT NULL,
  `createdAt` DATETIME NOT NULL DEFAULT NOW(),
  PRIMARY KEY (`id`)
);
```

---

## Phase 2: Entity 및 Repository 구현

### 2.1 Entity 클래스 생성
- [ ] `User.java` - tb_user 테이블 매핑
  - 필드: userId, loginId, password, userName, socialLoginYn, provider, providerId 등
  - Enum: RegisteredPath, WithdrawalType
  - 비즈니스 메서드: updateLastLoginAt(), updateUserName() 등

- [ ] `AdminAccount.java` - tb_adminAccount 테이블 매핑
  - 필드: adminId, loginId, password, name, level 등
  - 비즈니스 메서드: delete(), isDeleted() 등

- [ ] `RefreshToken.java` - tb_refresh_token 테이블 매핑
  - 필드: id, token, userType, userId, expiryDate, createdAt
  - 비즈니스 메서드: isExpired(), updateToken() 등

### 2.2 Repository 인터페이스 생성
- [ ] `UserRepository.java`
  - findByLoginId()
  - existsByLoginId()
  - findByProviderAndProviderId() (소셜 로그인용)

- [ ] `AdminAccountRepository.java`
  - findByLoginId()
  - findByLoginIdAndDeletedAtIsNull() (삭제되지 않은 관리자)

- [ ] `RefreshTokenRepository.java`
  - findByToken()
  - findByUserTypeAndUserId()
  - deleteByToken()
  - deleteByUserTypeAndUserId()
  - deleteByExpiryDateBefore() (만료된 토큰 정리)

---

## Phase 3: DTO 및 공통 클래스 구현

### 3.1 Request DTO
- [ ] `SignupRequest.java` - 회원가입 요청
- [ ] `LoginRequest.java` - 로그인 요청
- [ ] `RefreshTokenRequest.java` - 토큰 갱신 요청

### 3.2 Response DTO
- [ ] `TokenResponse.java` - 토큰 응답
- [ ] `ApiResponse.java` - 공통 API 응답 형식
- [ ] `UserInfoResponse.java` - 사용자 정보 응답

### 3.3 Exception 클래스
- [ ] `CustomException.java` - 커스텀 예외
- [ ] `ErrorCode.java` - 에러 코드 Enum
- [ ] `GlobalExceptionHandler.java` - 전역 예외 핸들러 (@RestControllerAdvice)

---

## Phase 4: Security 및 JWT 구현

### 4.1 JWT 관련 클래스
- [ ] `JwtProperties.java` - JWT 설정 값을 읽어오는 Configuration 클래스
  - @ConfigurationProperties(prefix = "jwt")
  - secret, accessTokenExpiration, refreshTokenExpiration

- [ ] `JwtTokenProvider.java` - JWT 생성 및 검증
  - generateAccessToken(userId, userType)
  - generateRefreshToken(userId, userType)
  - validateToken(token)
  - getUserIdFromToken(token)
  - getUserTypeFromToken(token)

### 4.2 Security Filter
- [ ] `JwtAuthenticationFilter.java` - JWT 인증 필터
  - Authorization 헤더에서 토큰 추출
  - 토큰 검증
  - SecurityContext에 인증 정보 설정

### 4.3 OAuth2 Handler
- [ ] `OAuth2SuccessHandler.java` - OAuth2 로그인 성공 핸들러
  - 소셜 로그인 성공 시 사용자 정보 추출
  - 자동 회원가입 또는 기존 사용자 조회
  - JWT 토큰 생성 및 반환

### 4.4 Security Configuration
- [ ] `SecurityConfig.java` - Spring Security 설정
  - SecurityFilterChain 빈 설정
  - 경로별 접근 권한 설정 (permitAll, hasRole 등)
  - JWT 필터 추가
  - OAuth2 로그인 설정
  - PasswordEncoder 빈 (BCrypt)

---

## Phase 5: Service 계층 구현

### 5.1 RefreshTokenService
- [ ] `RefreshTokenService.java`
  - createRefreshToken(userId, userType) - Refresh Token 생성 및 저장
  - validateRefreshToken(token) - Refresh Token 검증
  - deleteRefreshToken(token) - 로그아웃 시 토큰 삭제
  - deleteExpiredTokens() - 만료된 토큰 정리 (스케줄링 가능)

### 5.2 UserAuthService
- [ ] `UserAuthService.java`
  - signup(SignupRequest) - 회원가입
    - 이메일 중복 체크
    - 비밀번호 해싱 (BCrypt)
    - User 엔티티 저장
  - login(LoginRequest) - 로그인
    - 사용자 조회
    - 비밀번호 검증
    - JWT 토큰 생성
    - Refresh Token 저장
    - lastLoginAt 업데이트
  - refresh(refreshToken) - 토큰 갱신
    - Refresh Token 검증
    - 새로운 Access Token 생성
  - logout(refreshToken) - 로그아웃
    - Refresh Token 삭제

### 5.3 AdminAuthService
- [ ] `AdminAuthService.java`
  - login(LoginRequest) - 관리자 로그인
    - 관리자 조회 (deletedAt이 null인 것만)
    - 비밀번호 검증
    - JWT 토큰 생성 (userType: ADMIN)
    - Refresh Token 저장
  - refresh(refreshToken) - 관리자 토큰 갱신
  - logout(refreshToken) - 관리자 로그아웃

### 5.4 UserService
- [ ] `UserService.java`
  - getUserInfo(userId) - 사용자 정보 조회
  - updateUserInfo(userId, request) - 사용자 정보 수정

---

## Phase 6: Controller 계층 구현

### 6.1 UserAuthController
- [ ] `UserAuthController.java` - `/api/auth/**`
  - POST /signup - 회원가입
  - POST /login - 로그인
  - POST /refresh - 토큰 갱신
  - POST /logout - 로그아웃

### 6.2 AdminAuthController
- [ ] `AdminAuthController.java` - `/api/admin/auth/**`
  - POST /login - 관리자 로그인
  - POST /refresh - 관리자 토큰 갱신
  - POST /logout - 관리자 로그아웃

### 6.3 UserController
- [ ] `UserController.java` - `/api/user/**`
  - GET /me - 내 정보 조회 (인증 필요)
  - PATCH /me - 내 정보 수정 (인증 필요)

### 6.4 AdminController (선택사항)
- [ ] `AdminController.java` - `/api/admin/**`
  - GET /users - 사용자 목록 조회 (페이징)
  - GET /users/{userId} - 특정 사용자 조회

---

## Phase 7: 테스트 및 검증

### 7.1 단위 테스트
- [ ] Service 계층 테스트
  - UserAuthService 테스트 (Mockito)
  - AdminAuthService 테스트
  - RefreshTokenService 테스트

### 7.2 통합 테스트
- [ ] Controller 테스트 (MockMvc 사용)
  - 회원가입 API 테스트
  - 로그인 API 테스트
  - 토큰 갱신 API 테스트
  - 인증이 필요한 API 테스트

### 7.3 수동 테스트
- [ ] Postman 또는 cURL로 API 테스트
  - 회원가입 → 로그인 → 내 정보 조회
  - 소셜 로그인 (Google, Kakao)
  - 관리자 로그인 → 사용자 목록 조회
  - 토큰 갱신 및 로그아웃

---

## Phase 8: OAuth2 소셜 로그인 통합 및 테스트

### 8.1 OAuth2 설정
- [ ] Google Developer Console에서 OAuth2 Client 생성
  - Client ID, Client Secret 발급
  - Redirect URI 설정: `http://localhost:8080/login/oauth2/code/google`

- [ ] Kakao Developers에서 앱 생성
  - REST API 키, Client Secret 발급
  - Redirect URI 설정: `http://localhost:8080/login/oauth2/code/kakao`

### 8.2 소셜 로그인 테스트
- [ ] Google 로그인 테스트
  - `/oauth2/authorization/google` 접속
  - Google 계정으로 로그인
  - JWT 토큰 발급 확인
  - 자동 회원가입 확인

- [ ] Kakao 로그인 테스트
  - `/oauth2/authorization/kakao` 접속
  - Kakao 계정으로 로그인
  - JWT 토큰 발급 확인

---

## Phase 9: 추가 기능 및 개선 (선택사항)

### 9.1 보안 강화
- [ ] HTTPS 설정 (프로덕션)
- [ ] CORS 설정 (WebConfig)
- [ ] Rate Limiting (요청 횟수 제한)
- [ ] IP Whitelisting (관리자 API)

### 9.2 편의 기능
- [ ] 비밀번호 재설정 (이메일 인증)
- [ ] 이메일 인증 (회원가입 시)
- [ ] 중복 로그인 방지
- [ ] 로그인 이력 저장

### 9.3 모니터링 및 로깅
- [ ] 로그 설정 (Logback)
- [ ] 에러 로그 수집 (Sentry 등)
- [ ] API 호출 로그

---

## 체크리스트 요약

### 필수 구현
1. ✅ Phase 1: 기본 설정 완료
2. ✅ Phase 2: Entity/Repository 완료
3. ✅ Phase 3: DTO/Exception 완료
4. ✅ Phase 4: Security/JWT 완료
5. ✅ Phase 5: Service 완료
6. ✅ Phase 6: Controller 완료
7. ✅ Phase 7: 테스트 완료
8. ✅ Phase 8: OAuth2 완료

### 선택사항
- Phase 9: 추가 기능 (필요시 구현)

---

## 다음 단계

1. **Phase 1**부터 시작하여 하나씩 체크하면서 진행
2. 각 Phase가 완료되면 간단한 테스트로 동작 확인
3. 문제가 생기면 해당 Phase로 돌아가서 수정
4. 모든 Phase가 완료되면 전체 통합 테스트

**추천 순서**: Phase 1 → Phase 2 → Phase 4 → Phase 3 → Phase 5 → Phase 6 → Phase 7 → Phase 8

핵심부터 구현하고, DTO나 예외 처리는 필요할 때 만드는 방식이 효율적입니다!
