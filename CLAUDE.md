# Claude Code 작업 노트

> Spring Boot 회원가입/로그인 시스템 개발 프로젝트

**마지막 업데이트**: 2026-01-05
**현재 진행 상황**: JWT 토큰 관리 방식 검토 및 개선 진행 중

---

## 📋 프로젝트 개요

- **프로젝트명**: Backend - 인증 시스템
- **기술 스택**: Spring Boot 3.5.7, Java 21, MySQL, Redis, JWT
- **주요 기능**:
  - 일반 로그인 (loginId/비밀번호)
  - JWT 기반 인증 (Access Token + Refresh Token)
  - Redis 기반 토큰 관리

---

## 📝 작업 기록

### 2025-11-24

#### ✅ Phase 0: 프로젝트 초기 설정
1. **프로젝트 구조 확인**
   - Spring Boot 3.5.7 프로젝트 기본 구조 확인
   - docs 폴더 내 설계 문서 확인
   - build.gradle 의존성 확인

2. **Claude Code Skills 개념 학습**
   - Agent Skills 공식 문서 검토
   - Skills 작동 방식 이해 (model-invoked)
   - SKILL.md 작성 방법 학습

#### ✅ 완료: Claude Code Agent Skills 3개 생성

회원가입/로그인 시스템 개발 효율성을 높이기 위해 3개의 전문화된 스킬을 생성했습니다.

#### ✅ Phase 1: Entity 및 Repository 생성 완료

1. **SQL 테이블 수정**
   - tb_refresh_token 테이블 추가 (sql/Admin-Page-Table.sql)
   - userId, adminId를 외래키로 연결
   - userType ENUM으로 사용자/관리자 구분

2. **Entity 클래스 생성 (spring-entity-generator 스킬 사용)**
   - User.java (TB_USER 테이블 매핑)

3. **Repository 인터페이스 생성**
   - UserRepository.java

#### ✅ Phase 2: JWT 인증 구현 완료

1. **JWT 라이브러리 추가 (jwt-token-helper 스킬 사용)**
   - build.gradle에 jjwt 0.12.3 의존성 추가
   - io.jsonwebtoken:jjwt-api
   - io.jsonwebtoken:jjwt-impl
   - io.jsonwebtoken:jjwt-jackson

2. **JwtTokenProvider 생성**
   - Access Token / Refresh Token 생성 메서드
   - 토큰 검증 로직 (서명, 만료 시간, 형식)
   - 토큰에서 정보 추출 (loginId, role, userType)
   - loginId 기반으로 동작 (email 대신)
   - userType (USER/ADMIN) 구분 지원

3. **JwtAuthenticationFilter 생성**
   - OncePerRequestFilter 상속
   - Authorization 헤더에서 Bearer 토큰 추출
   - JWT 검증 및 SecurityContext에 인증 정보 저장
   - 권한(Role) 기반 접근 제어 준비

4. **TokenResponse DTO 생성**
   - accessToken, refreshToken 응답 구조
   - tokenType (Bearer) 포함
   - expiresIn (만료 시간) 포함
   - userType (USER/ADMIN) 구분

5. **application.properties 설정**
   - jwt.secret: 256비트 Secret Key
   - jwt.access-token-validity: 1시간 (3600000ms)
   - jwt.refresh-token-validity: 7일 (604800000ms)

6. **jjwt 0.12.3 최신 API로 업데이트**
   - deprecated된 API를 최신 버전으로 수정
   - `setSubject()` → `subject()`
   - `setIssuedAt()` → `issuedAt()`
   - `setExpiration()` → `expiration()`
   - `signWith(key, algorithm)` → `signWith(key)`
   - `parserBuilder()` → `parser()`
   - `setSigningKey()` → `verifyWith()`
   - `parseClaimsJws()` → `parseSignedClaims()`
   - `getBody()` → `getPayload()`

#### ✅ Phase 3: Spring Security 설정 완료

1. **SecurityConfig 생성 (spring-security-config 스킬 사용)**
   - @Configuration, @EnableWebSecurity 설정
   - JwtAuthenticationFilter 의존성 주입

2. **SecurityFilterChain 설정**
   - CSRF 비활성화 (JWT 사용으로 불필요)
   - 세션 관리: STATELESS (JWT 기반 인증)
   - JWT 필터를 UsernamePasswordAuthenticationFilter 이전에 등록

3. **URL별 권한 설정**
   - `/api/auth/**`: 인증 불필요 (회원가입, 로그인)
   - `/api/public/**`: 인증 불필요 (공개 API)
   - `/api/admin/**`: ADMIN 권한 필요
   - `/api/super-admin/**`: SUPER_ADMIN 권한 필요
   - `/api/user/**`: USER 또는 ADMIN 권한 필요
   - 나머지 URL: 인증 필요

4. **CORS 설정**
   - 허용 Origin: localhost:3000, localhost:5173, localhost:8080
   - 허용 메서드: GET, POST, PUT, DELETE, PATCH, OPTIONS
   - 허용 헤더: 모든 헤더 (*)
   - Credentials 허용: true (쿠키, Authorization 헤더 포함)
   - Preflight 캐시: 3600초

5. **PasswordEncoder Bean**
   - BCryptPasswordEncoder 사용
   - 비밀번호 암호화 강도: 10 (기본값)

### 2025-11-26

#### ✅ Phase 3.5: 테스트 코드 작성 및 검증 완료

Phase 4로 넘어가기 전, 지금까지 작업한 Phase 1-3의 기능들을 검증하기 위한 테스트 코드를 작성했습니다.

1. **JwtTokenProvider 단위 테스트 작성**
   - 파일: `src/test/java/com/BO/admin/security/jwt/JwtTokenProviderTest.java`
   - 총 23개 테스트 케이스
   - 테스트 내용:
     - Access Token / Refresh Token 생성 테스트
     - 토큰에서 정보 추출 (loginId, role, userType)
     - 토큰 유효성 검증 (유효한 토큰, 잘못된 토큰, 빈 토큰)
     - 토큰 만료 처리 및 만료 확인
     - 다양한 사용자 타입 테스트 (USER, ADMIN, SUPER_ADMIN)
     - 토큰 타입 구분 (access/refresh)
     - 만료 시간 확인

2. **Repository 통합 테스트 작성**
   - 파일: `src/test/java/com/BO/admin/repository/RepositoryIntegrationTest.java`
   - 총 19개 테스트 케이스
   - H2 in-memory DB 사용 (@DataJpaTest)
   - 테스트 내용:
     - **UserRepository (8개)**:
       - 일반 사용자 / 소셜 로그인 사용자 저장 및 조회
       - loginId로 찾기, 존재 여부 확인
       - 탈퇴 처리 및 탈퇴한 사용자 필터링
     - **AdminRepository (5개)**:
       - 관리자 저장 및 조회 (ADMIN, SUPER_ADMIN, MANAGER)
       - loginId로 찾기, 존재 여부 확인
       - 삭제된 관리자 필터링
     - **RefreshTokenRepository (6개)**:
       - USER / ADMIN Refresh Token 저장 및 조회
       - 토큰 삭제 (userId, token 기준)
       - 만료된 토큰 삭제
       - 토큰 만료 여부 확인

3. **SecurityConfig 통합 테스트 작성**
   - 파일: `src/test/java/com/BO/admin/config/SecurityConfigIntegrationTest.java`
   - 총 20개 테스트 케이스
   - MockMvc 사용 (@SpringBootTest, @AutoConfigureMockMvc)
   - 테스트 내용:
     - **URL 권한 검증 (6개)**:
       - `/api/auth/**`, `/api/public/**` - 인증 불필요
       - `/api/user/**` - USER 또는 ADMIN 권한 필요
       - `/api/admin/**` - ADMIN 권한 필요
       - `/api/super-admin/**` - SUPER_ADMIN 권한 필요
     - **JWT 인증 테스트 (8개)**:
       - USER 권한으로 /api/user/** 접근
       - ADMIN 권한으로 /api/user/**, /api/admin/** 접근
       - SUPER_ADMIN 권한으로 /api/super-admin/** 접근
       - 권한 부족 시 403 Forbidden 반환
       - 잘못된 토큰, Bearer 누락 시 401 Unauthorized 반환
     - **CORS 테스트 (4개)**:
       - Preflight 요청 처리 (localhost:3000, localhost:5173)
       - 허용된 HTTP 메서드 확인
       - Authorization 헤더 노출 확인
     - **기타 Security 테스트 (2개)**:
       - CSRF 비활성화 확인
       - STATELESS 세션 확인

4. **테스트 설정 파일 생성**
   - 파일: `src/test/resources/application-test.properties`
   - H2 in-memory DB 설정
   - JWT 테스트용 Secret Key 설정
   - 로깅 레벨 설정 (DEBUG)

5. **SecurityConfig 수정**
   - 인증 실패 시 **401 Unauthorized** 반환하도록 수정
   - `HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)` 추가
   - REST API 표준에 맞게 개선

6. **테스트 실행 결과**
   ```bash
   ./gradlew test
   ```
   - **결과**: 전체 62개 테스트 모두 성공 ✅
   - JwtTokenProviderTest: 23개 통과
   - RepositoryIntegrationTest: 19개 통과
   - SecurityConfigIntegrationTest: 20개 통과

### 2025-12-29

#### 🔄 프로젝트 구조 리팩토링

기존 복잡한 구조를 단순화하고, Refresh Token 저장소를 DB에서 Redis로 변경했습니다.

1. **Entity 간소화**
   - ❌ `Admin.java` 삭제 (관리자 기능 제거)
   - ❌ `AdminLevel.java` 삭제
   - ❌ `RefreshToken.java` 삭제 (Redis로 대체)
   - ❌ `RegisteredPath.java` 삭제
   - ❌ `UserType.java` 삭제
   - ❌ `WithdrawalType.java` 삭제

2. **User Entity 재설계** (`src/main/java/com/BO/admin/entity/User.java`)
   - 테이블명: `TB_USER` (새로운 구조)
   - 필드 간소화:
     - `userSeq` (INT, PK) - 사용자 고유 번호
     - `loginId` (VARCHAR) - 로그인 ID
     - `password` (VARCHAR) - 암호화된 비밀번호
     - `userName` (VARCHAR) - 사용자 이름
     - `language` (VARCHAR) - 언어 설정 (기본값: ko)
     - `subscribeInService` (VARCHAR) - 구독 여부 (Y/N)
     - `accessToken` (VARCHAR) - 현재 활성 Access Token
     - `registerDate` (DATETIME) - 가입일시
     - `modifyDate` (DATETIME) - 수정일시

3. **Repository 간소화**
   - ❌ `AdminRepository.java` 삭제
   - ❌ `RefreshTokenRepository.java` 삭제 (Redis로 대체)
   - ✅ `UserRepository.java` 수정

4. **새로운 SQL 파일 생성** (`sql/TB_USER.sql`)
   ```sql
   CREATE TABLE TB_USER (
       user_seq    INT AUTO_INCREMENT PRIMARY KEY,
       login_id    VARCHAR(50) NOT NULL UNIQUE,
       password    VARCHAR(255) NOT NULL,
       user_name   VARCHAR(100) NOT NULL,
       language    VARCHAR(10) DEFAULT 'ko',
       subscribe_in_service VARCHAR(1) DEFAULT 'N',
       access_token VARCHAR(500),
       register_date DATETIME DEFAULT CURRENT_TIMESTAMP,
       modify_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
   );
   ```

#### ✅ Redis 기반 Refresh Token 관리

DB 대신 Redis를 사용하여 Refresh Token을 관리합니다.

1. **RedisConfig.java 생성** (`src/main/java/com/BO/admin/config/RedisConfig.java`)
   - Lettuce 클라이언트 사용
   - `RedisTemplate<String, String>` 설정
   - StringRedisSerializer로 Key/Value 직렬화

2. **RefreshTokenService.java 생성** (`src/main/java/com/BO/admin/service/RefreshTokenService.java`)
   - `saveRefreshToken()` - Redis에 Refresh Token 저장
   - `getRefreshToken()` - Refresh Token 조회
   - `deleteRefreshToken()` - Refresh Token 삭제 (로그아웃)
   - `validateRefreshToken()` - 토큰 유효성 확인
   - Key 패턴: `refresh:{loginId}`
   - TTL: 7일 (`jwt.refresh-token-validity` 기반)

3. **build.gradle에 Redis 의존성 추가**
   ```gradle
   implementation 'org.springframework.boot:spring-boot-starter-data-redis-reactive'
   ```

#### ✅ Phase 5: Service 계층 구현

1. **AuthService.java 생성** (`src/main/java/com/BO/admin/service/AuthService.java`)
   - `signup()` - 회원가입 (토큰 발급 없음, 로그인 필요)
     - 중복 체크, 비밀번호 암호화, User 저장
   - `login()` - 로그인 + JWT 발급
     - 사용자 조회, 비밀번호 확인
     - Access Token 생성 → User 테이블에 저장
     - Refresh Token 생성 → Redis에 저장
   - `logout()` - 로그아웃
     - Redis에서 Refresh Token 삭제

#### ✅ Phase 6: Controller 계층 구현 (일부)

1. **AuthController.java 생성** (`src/main/java/com/BO/admin/controller/AuthController.java`)
   - `POST /api/auth/signup` - 회원가입
     - 요청: `{ loginId, password, userName, language, subscribeInService }`
     - 응답: `{ message, loginId, userName }`
   - `POST /api/auth/login` - 로그인
     - 요청: `{ loginId, password }`
     - 응답: `TokenResponse` (accessToken, refreshToken, tokenType, expiresIn, userType)
   - `POST /api/auth/logout` - 로그아웃
     - 헤더: `Authorization: Bearer {token}`
     - 응답: `{ message }`
   - `GET /api/auth/health` - 헬스 체크

2. **DTO 추가**
   - `LoginRequest.java` - 로그인 요청 (loginId, password)
   - `SignupRequest.java` - 회원가입 요청 (loginId, password, userName, language, subscribeInService)

#### ✅ Swagger API 문서화

1. **SwaggerConfig.java 생성** (`src/main/java/com/BO/admin/config/SwaggerConfig.java`)
   - OpenAPI 3.0 설정
   - JWT Bearer 인증 스키마 설정
   - API 정보: Admin API v1.0

2. **SecurityConfig.java 수정**
   - Swagger UI 경로 인증 제외 추가:
     - `/swagger-ui/**`
     - `/swagger-ui.html`
     - `/v3/api-docs/**`
     - `/swagger-resources/**`

3. **build.gradle에 Swagger 의존성 추가**
   ```gradle
   implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6'
   ```

4. **접속 URL**
   - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
   - OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### 2026-01-05

#### 🔍 JWT 토큰 관리 방식 검토

1. **현재 구현 분석**
   - Access Token: MySQL DB (tb_user.access_token)에 저장
   - Refresh Token: Redis에 저장 (TTL 적용)
   - 문제점: Access Token 만료 후에도 로그아웃 가능 (parseClaims에서 ExpiredJwtException 처리)

2. **JWT 토큰 관리 방식 논의**
   - **Stateless 방식**: 토큰을 서버에 저장하지 않음 (일반적인 JWT 방식)
   - **Stateful 방식**: Access Token + Refresh Token 모두 Redis에 저장
   - Access Token을 MySQL 대신 Redis로 이동 검토 중

3. **다음 작업 예정**
   - `/api/auth/refresh` API 구현 (Refresh Token으로 Access Token 재발급)
   - Access Token 저장소를 MySQL → Redis로 변경 검토
   - 토큰 즉시 무효화 기능 구현

4. **코드 정리**
   - `@NonNull` 어노테이션 추가 (IDE 경고 해결)
     - JwtAuthenticationFilter: `@NonNull` 파라미터 추가
     - RedisConfig: 기본값 설정
     - AuthService: `Objects.requireNonNull()` 적용

#### 📁 현재 프로젝트 구조 (2026-01-05 기준)

```
src/main/java/com/BO/admin/
├── AdminApplication.java
├── config/
│   ├── RedisConfig.java          # ✅ NEW - Redis 설정
│   ├── SecurityConfig.java       # ✅ 수정 - Swagger 경로 추가
│   └── SwaggerConfig.java        # ✅ NEW - Swagger 설정
├── controller/
│   └── AuthController.java       # ✅ NEW - 인증 API
├── dto/auth/
│   ├── LoginRequest.java         # ✅ NEW - 로그인 요청
│   ├── SignupRequest.java        # ✅ NEW - 회원가입 요청
│   └── TokenResponse.java        # 기존
├── entity/
│   └── User.java                 # ✅ 수정 - 간소화
├── repository/
│   └── UserRepository.java       # ✅ 수정
├── security/jwt/
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenProvider.java
└── service/
    ├── AuthService.java          # ✅ NEW - 인증 서비스
    └── RefreshTokenService.java  # ✅ NEW - Redis 토큰 관리
```

---

## 🛡️ 생성된 Security 설정 상세

### SecurityConfig

**위치**: `src/main/java/com/BO/admin/config/SecurityConfig.java`

**주요 Bean**:
- `securityFilterChain(HttpSecurity)` - Security 필터 체인 설정
- `corsConfigurationSource()` - CORS 정책 설정
- `passwordEncoder()` - 비밀번호 암호화

**Security 특징**:
- **JWT 기반 인증**: 세션 없이 토큰으로 인증 처리
- **CORS 지원**: 프론트엔드와의 통신 허용
- **Role 기반 권한 관리**: USER, ADMIN, SUPER_ADMIN 등 역할별 접근 제어
- **Public API 지원**: 인증 없이 접근 가능한 엔드포인트 제공

**필터 순서**:
```
1. JwtAuthenticationFilter (JWT 토큰 검증 및 인증)
2. UsernamePasswordAuthenticationFilter (기본 인증)
3. ... (기타 Security 필터들)
```

**URL 권한 매트릭스**:
| URL 패턴 | 권한 요구사항 | 설명 |
|---------|------------|------|
| `/api/auth/**` | None | 회원가입, 로그인 |
| `/api/public/**` | None | 공개 API |
| `/api/user/**` | USER | 사용자 기능 |
| `/error`, `/favicon.ico` | None | 시스템 |
| 나머지 모든 URL | Authenticated | 인증 필요 |

---

## 🔐 생성된 JWT 상세

### JwtTokenProvider

**위치**: `src/main/java/com/BO/admin/security/jwt/JwtTokenProvider.java`

**주요 메서드**:
- `createAccessToken(String loginId, String role, String userType)` - Access Token 생성
- `createRefreshToken(String loginId, String userType)` - Refresh Token 생성
- `validateToken(String token)` - 토큰 유효성 검증
- `getLoginIdFromToken(String token)` - 토큰에서 로그인 ID 추출
- `getRoleFromToken(String token)` - 토큰에서 역할 추출
- `getUserTypeFromToken(String token)` - 토큰에서 사용자 타입 추출
- `isTokenExpired(String token)` - 토큰 만료 여부 확인
- `getTokenType(String token)` - 토큰 타입 확인 (access/refresh)

**API 버전**: jjwt 0.12.3 최신 API 사용
- 모든 deprecated 메서드를 최신 API로 교체
- `Jwts.builder()`: `subject()`, `issuedAt()`, `expiration()`, `signWith()` 사용
- `Jwts.parser()`: `verifyWith()`, `parseSignedClaims()`, `getPayload()` 사용

**Claims 구조**:
```json
{
  "sub": "loginId",
  "role": "USER",
  "userType": "USER",
  "type": "access | refresh",
  "iat": 1234567890,
  "exp": 1234567890
}
```

### JwtAuthenticationFilter

**위치**: `src/main/java/com/BO/admin/security/jwt/JwtAuthenticationFilter.java`

**동작 방식**:
1. HTTP 요청의 Authorization 헤더에서 Bearer 토큰 추출
2. JwtTokenProvider로 토큰 유효성 검증
3. 토큰에서 loginId, role, userType 추출
4. UsernamePasswordAuthenticationToken 생성
5. SecurityContextHolder에 인증 정보 저장

**필터 순서**: `UsernamePasswordAuthenticationFilter` 이전에 실행

### TokenResponse DTO

**위치**: `src/main/java/com/BO/admin/dto/auth/TokenResponse.java`

**필드**:
- `accessToken` (String) - Access Token
- `refreshToken` (String) - Refresh Token
- `tokenType` (String) - "Bearer" 고정
- `expiresIn` (Long) - Access Token 만료 시간 (ms)
- `userType` (String) - 사용자 타입 (USER)

**사용 예시**:
```java
TokenResponse response = TokenResponse.of(
    accessToken,
    refreshToken,
    3600000L,
    "USER"
);
```

---

## 📦 생성된 Entity 상세

### User Entity (tb_user)

**테이블**: `tb_user`
**PK**: `userId` (BIGINT UNSIGNED)

**주요 필드**:
- `loginId` (VARCHAR) - 로그인 ID, NOT NULL, 중복 체크 필요
- `userName` (VARCHAR) - 사용자 이름, NOT NULL
- `password` (VARCHAR) - 암호화된 비밀번호, NOT NULL
- `socialLoginYn` (BOOLEAN) - 소셜 로그인 여부, DEFAULT false
- `registeredPath` (ENUM) - 가입 경로 (EMAIL/SOCIAL), DEFAULT EMAIL
- `withdrawalType` (ENUM) - 탈퇴 유형 (SELF/SYSTEM), NULL
- `language` (VARCHAR) - 언어 설정, DEFAULT 'ENG'
- `subscribeInService` (BOOLEAN) - 서비스 구독 여부, DEFAULT false

**타임스탬프**:
- `registeredAt` (DATETIME) - 가입일시, @PrePersist로 자동 설정
- `updatedAt` (DATETIME) - 수정일시, @PreUpdate로 자동 갱신
- `lastLoginAt` (DATETIME) - 마지막 로그인 일시
- `deactivatedAt` (DATETIME) - 비활성화 일시
- `withdrawalAt` (DATETIME) - 탈퇴 일시

**비즈니스 메서드**:
- `isSocialUser()` - 소셜 로그인 사용자 여부
- `isWithdrawn()` - 탈퇴한 사용자 여부
- `isDeactivated()` - 비활성화된 사용자 여부

---

## 🛠 생성된 Skills 상세

### 1. spring-entity-generator

**파일 위치**: `.claude/skills/spring-entity-generator/SKILL.md`

**목적**: JPA Entity와 Repository를 빠르고 정확하게 생성

**생성 일시**: 2025-11-24

**주요 기능**:
- ✅ Entity 클래스 자동 생성 (JPA 어노테이션 포함)
- ✅ Repository 인터페이스 자동 생성
- ✅ Lombok 어노테이션 자동 추가 (@Getter, @Setter, @Builder 등)
- ✅ 관계 설정 (@OneToMany, @ManyToOne 등)
- ✅ Enum 타입 자동 생성
- ✅ 타임스탬프 필드 자동 추가 (@CreationTimestamp, @UpdateTimestamp)

**자동 발동 키워드**:
- "User 엔티티 만들어줘"
- "회원 Entity 만들어줘"

**생성되는 파일 예시**:
```
src/main/java/com/BO/admin/
├── entity/
│   └── User.java              # 사용자 엔티티
└── repository/
    └── UserRepository.java
```

**코드 생성 규칙**:
- 패키지: `com.BO.admin.entity`, `com.BO.admin.repository`
- 테이블명: snake_case 복수형 (예: users, oauth_users)
- ID: Long 타입, @GeneratedValue(IDENTITY)
- 필수 어노테이션: @Entity, @Table, @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor, @Builder
- Repository: JpaRepository<Entity, Long> 상속

**사용 도구 제한**: Read, Write, Glob, Grep (파일 수정 없이 읽기/쓰기만)

---

### 2. jwt-token-helper

**파일 위치**: `.claude/skills/jwt-token-helper/SKILL.md`

**목적**: JWT 토큰 관련 모든 로직을 자동으로 생성

**생성 일시**: 2025-11-24

**주요 기능**:
- ✅ Access Token / Refresh Token 생성 로직
- ✅ 토큰 검증 (서명, 만료 시간, 형식)
- ✅ 토큰에서 사용자 정보 추출 (이메일, 역할)
- ✅ application.properties JWT 설정 자동화
- ✅ Refresh Token Entity/Repository 생성 (선택)
- ✅ 토큰 필터 구현 (JwtAuthenticationFilter)

**자동 발동 키워드**:
- "JWT 토큰 생성 로직 만들어줘"
- "토큰 검증 필터 필요해"
- "Refresh Token 관리해줘"
- "JWT 인증 구현해줘"

**생성되는 파일 예시**:
```
src/main/java/com/BO/admin/
├── security/
│   └── jwt/
│       ├── JwtTokenProvider.java          # 토큰 생성/검증 핵심 클래스
│       └── JwtAuthenticationFilter.java   # JWT 인증 필터
├── dto/
│   └── auth/
│       └── TokenResponse.java             # 토큰 응답 DTO
├── entity/
│   └── RefreshToken.java                  # Refresh Token 엔티티 (선택)
└── repository/
    └── RefreshTokenRepository.java        # Refresh Token Repository (선택)
```

**핵심 클래스**:
- `JwtTokenProvider`: 토큰 생성, 검증, 파싱의 모든 로직 포함
  - createAccessToken()
  - createRefreshToken()
  - validateToken()
  - getEmailFromToken()
  - getRoleFromToken()

**보안 설정**:
- Secret Key: 최소 256비트 (HS256 알고리즘)
- Access Token 만료: 1시간 (3600000ms)
- Refresh Token 만료: 7일 (604800000ms)
- 프로덕션 환경: Secret Key를 환경 변수로 관리 권장

**필요한 의존성**:
```gradle
implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
```

**사용 도구 제한**: Read, Write, Glob, Grep

---

### 3. spring-security-config

**파일 위치**: `.claude/skills/spring-security-config/SKILL.md`

**목적**: Spring Security 전체 설정을 자동으로 구성

**생성 일시**: 2025-11-24

**주요 기능**:
- ✅ SecurityFilterChain 설정
- ✅ CORS 정책 설정 (프론트엔드 연동)
- ✅ JWT 필터 연동
- ✅ OAuth2 소셜 로그인 설정 (Google, Kakao)
- ✅ URL별 권한 관리 (ROLE 기반)
- ✅ 비밀번호 암호화 (BCryptPasswordEncoder)

**자동 발동 키워드**:
- "Spring Security 설정해줘"
- "OAuth2 로그인 구현해줘"
- "CORS 설정 필요해"
- "JWT 필터 연동해줘"
- "보안 설정해줘"

**생성되는 파일 예시**:
```
src/main/java/com/BO/admin/
├── config/
│   └── SecurityConfig.java                    # Security 핵심 설정
└── security/
    ├── jwt/
    │   └── JwtAuthenticationFilter.java       # JWT 인증 필터
    └── oauth/
        ├── CustomOAuth2UserService.java       # OAuth2 사용자 서비스
        ├── OAuth2SuccessHandler.java          # OAuth2 로그인 성공 핸들러
        ├── CustomOAuth2User.java              # OAuth2 사용자 객체
        └── OAuth2UserInfo.java                # OAuth2 정보 추상화
```

**핵심 설정**:
- **CSRF**: 비활성화 (JWT 사용으로 불필요)
- **세션**: STATELESS (JWT 사용으로 세션 미사용)
- **CORS**: 프론트엔드 Origin 허용 (localhost:3000, localhost:5173)

**URL별 권한 설정**:
```
/api/auth/**      → 인증 불필요 (회원가입, 로그인)
/api/user/**      → USER 권한 필요
그 외 모든 URL     → 인증 필요
```

**OAuth2 지원**:
- Google 로그인 연동
- Kakao 로그인 연동
- 로그인 성공 시 JWT 토큰 발급 후 프론트엔드로 리다이렉트

**필요한 의존성**:
```gradle
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
```

**사용 도구 제한**: Read, Write, Glob, Grep

---

## 🎯 Skills 사용 방법

### 자동 발동 (Model-Invoked)

Claude가 대화 문맥을 분석하여 **자동으로** 적절한 스킬을 선택하고 실행합니다.

**예시**:
```
사용자: "User 엔티티 만들어줘"
→ Claude가 spring-entity-generator 스킬을 자동으로 사용
→ User.java, UserRepository.java 생성

사용자: "JWT 토큰 생성 로직 추가해줘"
→ Claude가 jwt-token-helper 스킬을 자동으로 사용
→ JwtTokenProvider.java 등 생성

사용자: "Spring Security 설정해줘"
→ Claude가 spring-security-config 스킬을 자동으로 사용
→ SecurityConfig.java 등 생성
```

### 스킬 확인 명령어

```bash
# 사용 가능한 모든 스킬 보기
ls .claude/skills/

# 특정 스킬 내용 보기
cat .claude/skills/spring-entity-generator/SKILL.md
cat .claude/skills/jwt-token-helper/SKILL.md
cat .claude/skills/spring-security-config/SKILL.md
```

---

## 📊 개발 워크플로우

### ✅ Phase 0: 준비 단계 (완료)
- [x] 프로젝트 생성 및 의존성 설정
- [x] Claude Code Skills 3개 생성
  - [x] spring-entity-generator
  - [x] jwt-token-helper
  - [x] spring-security-config
- [x] CLAUDE.md 문서화

### ✅ Phase 1: Entity 및 Repository 생성 (완료)
- [x] User Entity 생성 (TB_USER 기반)
- [x] UserRepository 생성

### ✅ Phase 2: JWT 인증 구현 (완료)
- [x] JwtTokenProvider 생성 (jwt-token-helper 스킬 사용)
- [x] JwtAuthenticationFilter 생성
- [x] TokenResponse DTO 생성
- [x] build.gradle에 JWT 의존성 추가 (jjwt 0.12.3)
- [x] application.properties에 JWT 설정 추가

### ✅ Phase 3: Spring Security 설정 (완료)
- [x] SecurityConfig 생성 (spring-security-config 스킬 사용)
- [x] SecurityFilterChain 설정 (CSRF 비활성화, 세션 STATELESS)
- [x] CORS 정책 설정 (프론트엔드 Origin 허용)
- [x] PasswordEncoder Bean 등록 (BCrypt)
- [x] URL별 권한 설정 (permitAll, hasRole)
- [x] JWT 필터 연동 (UsernamePasswordAuthenticationFilter 이전)

### ✅ Phase 3.5: 테스트 코드 작성 및 검증 (완료) - 2025-11-26
- [x] JwtTokenProvider 단위 테스트 작성 (23개 테스트)
  - [x] Access Token / Refresh Token 생성 및 검증
  - [x] 토큰에서 정보 추출 (loginId, role, userType)
  - [x] 토큰 만료 처리 및 확인
  - [x] 다양한 사용자 타입 테스트
- [x] Repository 통합 테스트 작성
  - [x] UserRepository: 사용자 저장/조회
- [x] SecurityConfig 통합 테스트 작성 (20개 테스트)
  - [x] URL별 권한 검증
  - [x] JWT 인증 필터 동작 확인
  - [x] CORS 헤더 확인
  - [x] CSRF 비활성화, STATELESS 세션 확인
- [x] application-test.properties 설정 (H2 in-memory DB)
- [x] SecurityConfig 수정: 인증 실패 시 401 Unauthorized 반환
- [x] 전체 테스트 실행 및 통과 확인 (62개 테스트 모두 성공 ✅)

### ⏳ Phase 4: OAuth2 소셜 로그인 구현 (다음 단계)

#### 📝 시작 명령어
```
"spring-security-config 스킬을 사용해서 Google과 Kakao OAuth2 로그인을 구현해줘"
```

#### ✅ 작업 목록

1. **OAuth2UserInfo 인터페이스 및 구현체**
   - [ ] OAuth2UserInfo.java (인터페이스) 생성
     - 소셜 로그인 제공자별 사용자 정보 추상화
     - getProviderId(), getProvider(), getEmail(), getName() 메서드
   - [ ] GoogleOAuth2UserInfo.java 생성
     - Google 로그인 사용자 정보 처리
   - [ ] KakaoOAuth2UserInfo.java 생성
     - Kakao 로그인 사용자 정보 처리

2. **CustomOAuth2User 구현**
   - [ ] CustomOAuth2User.java 생성
     - OAuth2User 인터페이스 구현
     - loginId, userType, authorities 정보 포함
     - Spring Security Context에 저장될 사용자 객체

3. **CustomOAuth2UserService 구현**
   - [ ] CustomOAuth2UserService.java 생성
     - DefaultOAuth2UserService 상속
     - loadUser() 메서드 오버라이드
     - 소셜 로그인 사용자 정보 로드 및 가공
     - 신규 사용자는 자동 회원가입 처리
     - 기존 사용자는 정보 업데이트

4. **OAuth2SuccessHandler 구현**
   - [ ] OAuth2SuccessHandler.java 생성
     - SimpleUrlAuthenticationSuccessHandler 상속
     - 로그인 성공 시 JWT 토큰 생성
     - Access Token + Refresh Token 발급
     - 프론트엔드로 리다이렉트 (토큰 전달)

5. **SecurityConfig에 OAuth2 설정 추가**
   - [ ] SecurityConfig.java 수정
     - oauth2Login() 설정 추가
     - userInfoEndpoint에 CustomOAuth2UserService 연결
     - successHandler에 OAuth2SuccessHandler 연결

6. **application.properties 설정**
   - 1. mysql 설정
   - 2. jpa 설정
   - 3. redis 설정
   - 4. jwt 설정

7. **build.gradle 의존성 확인**
   - [ ] OAuth2 Client 의존성이 있는지 확인
     ```gradle
     implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
     ```

#### 📦 생성될 파일 목록
```
src/main/java/com/BO/admin/security/oauth/
├── OAuth2UserInfo.java              # 인터페이스
├── GoogleOAuth2UserInfo.java        # Google 사용자 정보
├── KakaoOAuth2UserInfo.java         # Kakao 사용자 정보
├── CustomOAuth2User.java            # OAuth2 사용자 객체
├── CustomOAuth2UserService.java     # OAuth2 사용자 서비스
└── OAuth2SuccessHandler.java        # 로그인 성공 핸들러
```

#### 🎯 Phase 4 완료 조건
- Google 로그인 시 자동 회원가입 및 JWT 토큰 발급
- Kakao 로그인 시 자동 회원가입 및 JWT 토큰 발급
- 로그인 성공 후 프론트엔드로 토큰과 함께 리다이렉트
- 소셜 로그인 사용자는 User 테이블에 저장 (socialLoginYn=true, registeredPath=SOCIAL)

### ⏳ Phase 5: Service 계층 구현
- [x] AuthService 구현 (회원가입, 로그인, 로그아웃)
- [x] RefreshTokenService 구현 (Redis 기반)
- [x] UserService 구현
- [x] TokenService 구현 (토큰 갱신)

### ⏳ Phase 6: Controller 계층 구현
- [x] AuthController (회원가입, 로그인, 로그아웃)
- [ ] UserController (사용자 정보 조회/수정)
- [ ] TokenController (토큰 갱신)

### ⏳ Phase 7: 테스트 및 배포
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성
- [ ] API 테스트 (Postman/Swagger)
- [ ] 배포 준비

---

## 🔧 필수 의존성 체크리스트

### build.gradle

```gradle
dependencies {
    // Spring Boot Core
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

    // Security
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'

    // JWT (버전: 0.12.3 - 최신)
    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'

    // Database
    runtimeOnly 'com.mysql:mysql-connector-j'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}
```

**현재 상태**: build.gradle 확인 필요

---

## ⚙️ 환경 설정 (application.properties)

### 기본 설정

```properties
# 서버 포트
server.port=8080

# 데이터베이스 연결
spring.datasource.url=jdbc:mysql://localhost:3306/admin_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your-password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA 설정
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### JWT 설정

```properties
# JWT Secret Key (최소 256비트 필요)
jwt.secret=your-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm-security

# Access Token 만료 시간: 1시간 (3600000ms)
jwt.access-token-validity=3600000

# Refresh Token 만료 시간: 7일 (604800000ms)
jwt.refresh-token-validity=604800000
```

### OAuth2 - Google 설정

```properties
spring.security.oauth2.client.registration.google.client-id=your-google-client-id
spring.security.oauth2.client.registration.google.client-secret=your-google-client-secret
spring.security.oauth2.client.registration.google.scope=profile,email
```

### OAuth2 - Kakao 설정

```properties
spring.security.oauth2.client.registration.kakao.client-id=your-kakao-client-id
spring.security.oauth2.client.registration.kakao.client-secret=your-kakao-client-secret
spring.security.oauth2.client.registration.kakao.redirect-uri={baseUrl}/login/oauth2/code/kakao
spring.security.oauth2.client.registration.kakao.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.kakao.scope=profile_nickname,account_email
spring.security.oauth2.client.registration.kakao.client-name=Kakao

spring.security.oauth2.client.provider.kakao.authorization-uri=https://kauth.kakao.com/oauth/authorize
spring.security.oauth2.client.provider.kakao.token-uri=https://kauth.kakao.com/oauth/token
spring.security.oauth2.client.provider.kakao.user-info-uri=https://kapi.kakao.com/v2/user/me
spring.security.oauth2.client.provider.kakao.user-name-attribute=id
```

### CORS 및 기타 설정

```properties
# OAuth2 로그인 성공 후 리다이렉트 URL
app.oauth2.redirect-uri=http://localhost:3000/auth/callback

# CORS 허용 Origin
app.cors.allowed-origins=http://localhost:3000,http://localhost:5173
```

**현재 상태**: application.properties 확인 필요

---

## 📁 예상 프로젝트 구조

```
admin/
├── .claude/
│   └── skills/                           # ✅ 생성 완료
│       ├── spring-entity-generator/
│       │   └── SKILL.md
│       ├── jwt-token-helper/
│       │   └── SKILL.md
│       └── spring-security-config/
│           └── SKILL.md
│
├── docs/                                 # ✅ 이미 존재
│   ├── 00-SpringBoot-기초-개념.md
│   ├── 01-프로젝트-개요.md
│   ├── 02-아키텍처-설계.md
│   ├── 03-API-엔드포인트-설계.md
│   └── 04-구현-TODO-리스트.md
│
├── sql/
│   └── Admin-Page-Table.sql
│
├── src/
│   ├── main/
│   │   ├── java/com/BO/admin/
│   │   │   ├── AdminApplication.java     # ✅ 이미 존재
│   │   │   │
│   │   │   ├── config/                   # ⏳ Phase 3에서 생성
│   │   │   │   └── SecurityConfig.java
│   │   │   │
│   │   │   ├── entity/                   # ⏳ Phase 1에서 생성
│   │   │   │   ├── User.java
│   │   │   │   ├── Admin.java
│   │   │   │   ├── OAuthUser.java
│   │   │   │   ├── RefreshToken.java
│   │   │   │   ├── UserRole.java
│   │   │   │   └── OAuthProvider.java
│   │   │   │
│   │   │   ├── repository/               # ⏳ Phase 1에서 생성
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── AdminRepository.java
│   │   │   │   ├── OAuthUserRepository.java
│   │   │   │   └── RefreshTokenRepository.java
│   │   │   │
│   │   │   ├── service/                  # ⏳ Phase 5에서 생성
│   │   │   │   ├── UserService.java
│   │   │   │   ├── AdminService.java
│   │   │   │   ├── AuthService.java
│   │   │   │   └── TokenService.java
│   │   │   │
│   │   │   ├── controller/               # ⏳ Phase 6에서 생성
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── UserController.java
│   │   │   │   ├── AdminController.java
│   │   │   │   └── TokenController.java
│   │   │   │
│   │   │   ├── security/                 # ⏳ Phase 2-4에서 생성
│   │   │   │   ├── jwt/
│   │   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   │   └── oauth/
│   │   │   │       ├── CustomOAuth2UserService.java
│   │   │   │       ├── OAuth2SuccessHandler.java
│   │   │   │       ├── CustomOAuth2User.java
│   │   │   │       └── OAuth2UserInfo.java
│   │   │   │
│   │   │   ├── dto/                      # ⏳ 각 Phase에서 생성
│   │   │   │   ├── auth/
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── SignupRequest.java
│   │   │   │   │   ├── TokenResponse.java
│   │   │   │   │   └── RefreshTokenRequest.java
│   │   │   │   └── user/
│   │   │   │       └── UserResponse.java
│   │   │   │
│   │   │   └── exception/                # ⏳ 나중에 생성
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       └── CustomExceptions.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties    # ✅ JWT 설정 완료
│   │       └── application-dev.properties
│   │
│   └── test/                             # ✅ Phase 3.5에서 작성 완료 (2025-11-26)
│       ├── java/com/BO/admin/
│       │   ├── security/
│       │   │   └── jwt/
│       │   │       └── JwtTokenProviderTest.java       # ✅ 23개 테스트
│       │   ├── repository/
│       │   │   └── RepositoryIntegrationTest.java      # ✅ 19개 테스트
│       │   ├── config/
│       │   │   └── SecurityConfigIntegrationTest.java  # ✅ 20개 테스트
│       │   └── AdminApplicationTests.java              # ✅ 기본 테스트
│       └── resources/
│           └── application-test.properties              # ✅ H2 DB 설정
│
├── build.gradle                          # ✅ JWT 의존성 추가 완료
├── CLAUDE.md                             # ✅ 현재 파일
└── README.md                             # ✅ 이미 존재
```

---

## 🎯 현재 진행 상황 및 다음 할 일

### ✅ 완료된 작업 (2025-12-29 기준)
- ✅ Phase 0: Claude Code Skills 생성 (2025-11-24)
- ✅ Phase 1: Entity 및 Repository 생성 (2025-11-24)
- ✅ Phase 2: JWT 인증 구현 (2025-11-24)
  - jjwt 0.12.3 최신 API 적용
  - JwtTokenProvider, JwtAuthenticationFilter 구현
- ✅ Phase 3: Spring Security 설정 (2025-11-24)
  - JWT 필터 연동, CORS, URL별 권한 관리
- ✅ Phase 3.5: 테스트 코드 작성 및 검증 (2025-11-26)
  - JwtTokenProvider 단위 테스트 (23개)
  - Repository 통합 테스트 (19개)
  - SecurityConfig 통합 테스트 (20개)
  - 전체 62개 테스트 모두 통과 ✅
- ✅ **프로젝트 구조 리팩토링 (2025-12-29) ⭐ NEW**
  - Entity 간소화 (Admin, RefreshToken 등 삭제)
  - User Entity 재설계 (TB_USER)
  - Refresh Token → Redis 기반으로 변경
- ✅ **Phase 5: Service 계층 구현 (2025-12-29) ⭐ NEW**
  - AuthService (회원가입, 로그인, 로그아웃)
  - RefreshTokenService (Redis 기반 토큰 관리)
- ✅ **Phase 6: Controller 계층 구현 - 일부 (2025-12-29) ⭐ NEW**
  - AuthController (회원가입, 로그인, 로그아웃, 헬스체크)
  - Swagger API 문서화

### 🔄 다음 진행 작업

1. **토큰 갱신 API 구현**
   - `POST /api/auth/refresh` - Refresh Token으로 Access Token 재발급
   - Redis에서 Refresh Token 검증
   1.1 **구체적인 방안**
      ------
      방식 1: Stateless (일반적인 JWT 방식)

      토큰을 서버에 저장하지 않음

      | 항목          | 설명                                            |
      |---------------|-------------------------------------------------|
      | Access Token  | 저장 안 함 (토큰 자체로 검증)                   |
      | Refresh Token | Redis에 저장                                    |
      | 검증 방식     | 토큰 서명 + 만료시간만 확인                     |
      | 장점          | 빠름, DB/Redis 조회 없음, 확장성 좋음           |
      | 단점          | 로그아웃해도 Access Token 만료 전까지 사용 가능 |

      ---
      방식 2: Stateful (Redis에 둘 다 저장)

      Access Token + Refresh Token 둘 다 Redis에 저장

      | 항목          | 설명                                |
      |---------------|-------------------------------------|
      | Access Token  | Redis에 저장 (TTL: 1시간)           |
      | Refresh Token | Redis에 저장 (TTL: 7일)             |
      | 검증 방식     | 토큰 서명 + Redis에 존재하는지 확인 |
      | 장점          | 로그아웃 시 즉시 무효화 가능        |
      | 단점          | 매 요청마다 Redis 조회 필요         |

      ---
      현재 구조 vs 개선안

      현재: Access Token → MySQL (느림, 비효율)
            Refresh Token → Redis

      개선안 1 (Stateless): Access Token → 저장 안 함
                              Refresh Token → Redis

      개선안 2 (Stateful):  Access Token → Redis
                              Refresh Token → Redis
                              + MySQL에서 access_token 컬럼 제거

      ------
         - Stateless → 일반적인 JWT 방식, 성능 좋음
         - Stateful → 보안 강화 (즉시 무효화), Redis 의존

2. **UserController 구현**
   - `GET /api/user/me` - 내 정보 조회
   - `PUT /api/user/me` - 내 정보 수정

3. **OAuth2 소셜 로그인 구현 (Phase 4) - 선택**
   - Google, Kakao 로그인 연동
   - OAuth2UserInfo, CustomOAuth2UserService, OAuth2SuccessHandler 생성

4. **테스트 코드 업데이트**
   - 새로운 구조에 맞게 테스트 수정

---

## 💡 유용한 명령어

### Gradle 명령어

```bash
# 프로젝트 빌드
./gradlew build

# 프로젝트 실행
./gradlew bootRun

# 테스트 실행
./gradlew test

# 의존성 확인
./gradlew dependencies

# 빌드 캐시 정리
./gradlew clean
```

### Skills 관리

```bash
# 스킬 목록 보기
ls .claude/skills/

# 스킬 내용 보기
cat .claude/skills/spring-entity-generator/SKILL.md
cat .claude/skills/jwt-token-helper/SKILL.md
cat .claude/skills/spring-security-config/SKILL.md

# 스킬 디렉토리 구조 보기
find .claude/skills -type f -name "*.md"
```

### 데이터베이스

```bash
# MySQL 접속
mysql -u root -p

# 데이터베이스 생성 (MySQL에서 실행)
CREATE DATABASE admin_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 데이터베이스 확인
SHOW DATABASES;
USE admin_db;
SHOW TABLES;
```

---

## 📚 참고 문서

### 프로젝트 문서
- [Spring Boot 기초 개념](docs/00-SpringBoot-기초-개념.md)
- [프로젝트 개요](docs/01-프로젝트-개요.md)
- [아키텍처 설계](docs/02-아키텍처-설계.md)
- [API 엔드포인트 설계](docs/03-API-엔드포인트-설계.md)
- [구현 TODO 리스트](docs/04-구현-TODO-리스트.md)

### 외부 문서
- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Spring Security 공식 문서](https://spring.io/projects/spring-security)
- [JWT 공식 사이트](https://jwt.io/)
- [JJWT GitHub](https://github.com/jwtk/jjwt)

---

## 🔍 현재 상태 요약

### ✅ 완료된 작업

**Phase 0: 준비**
- [x] Claude Code Skills 3개 생성 (spring-entity-generator, jwt-token-helper, spring-security-config)
- [x] CLAUDE.md 문서화

**Phase 1-3: 기본 구조 (2025-11-24)**
- [x] Entity, Repository, JWT, Security 설정 완료

**Phase 3.5: 테스트 코드 (2025-11-26)**
- [x] 62개 테스트 모두 통과

**프로젝트 구조 리팩토링 (2025-12-29)**
- [x] Entity 간소화 (Admin, RefreshToken 등 삭제)
- [x] User Entity 재설계 → TB_USER 테이블
- [x] Refresh Token 저장소: DB → Redis 변경

**Phase 5: Service 계층 구현 (2025-12-29)**
- [x] AuthService (회원가입, 로그인, 로그아웃)
- [x] RefreshTokenService (Redis 기반)

**Phase 6: Controller 계층 구현 (2025-12-29) - 일부**
- [x] AuthController
  - [x] POST /api/auth/signup - 회원가입
  - [x] POST /api/auth/login - 로그인
  - [x] POST /api/auth/logout - 로그아웃
  - [x] GET /api/auth/health - 헬스체크
- [x] Swagger API 문서화 (SpringDoc OpenAPI)

**추가 설정 (2025-12-29)**
- [x] RedisConfig - Redis 연결 설정
- [x] SwaggerConfig - API 문서화
- [x] build.gradle에 Redis, Swagger 의존성 추가

### 🎯 현재 위치
**Phase 5-6 진행 중 (2026-01-05)** → 인증 API 구현 완료, JWT 토큰 관리 방식 개선 필요

### 📋 다음 단계
1. **토큰 갱신 API**
   - POST /api/auth/refresh - Refresh Token으로 Access Token 재발급

2. **Access Token 저장소 변경 검토**
   - MySQL → Redis로 이동
   - 또는 Stateless 방식으로 전환 (서버에 저장 안 함)

3. **UserController**
   - GET /api/user/me - 내 정보 조회
   - PUT /api/user/me - 내 정보 수정

---

## 💬 Skills 테스트 방법

Claude에게 다음과 같이 말하면 각 스킬이 자동으로 발동됩니다:

### 테스트 1: spring-entity-generator
```
"User 엔티티를 만들어줘. 이메일, 비밀번호, 이름, 역할 필드가 필요해."
```

### 테스트 2: jwt-token-helper
```
"JWT 토큰 생성 및 검증 로직을 추가해줘."
```

### 테스트 3: spring-security-config
```
"Spring Security 설정해줘. CORS도 함께 설정하고 JWT 필터 연동도 해줘."
```

---

**작성자**: Claude Code
**프로젝트 시작일**: 2025-11-24
**마지막 업데이트**: 2026-01-05 (JWT 토큰 관리 방식 검토, Admin 관련 내용 제거)
