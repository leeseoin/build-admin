---
name: jwt-token-helper
description: JWT(JSON Web Token) 생성, 검증, 파싱 유틸리티를 자동으로 생성합니다. Access Token, Refresh Token 관리, 토큰 유효성 검사, 사용자 정보 추출 기능을 포함합니다. JWT 인증이 필요할 때, 토큰 관련 로직을 구현할 때 사용하세요.
allowed-tools: Read, Write, Glob, Grep
---

# JWT Token Helper

Spring Boot에서 JWT 기반 인증을 구현하기 위한 유틸리티 클래스와 설정을 자동으로 생성하는 스킬입니다.

## 주요 기능

1. **JWT 토큰 생성**
   - Access Token 생성
   - Refresh Token 생성
   - 사용자 정보 기반 Claims 설정

2. **JWT 토큰 검증**
   - 서명 검증
   - 만료 시간 검증
   - 토큰 형식 검증

3. **토큰에서 정보 추출**
   - 사용자 이메일/ID 추출
   - Claims 파싱
   - 만료 시간 확인

4. **설정 관리**
   - application.properties에 JWT 설정 추가
   - Secret Key 관리
   - 토큰 만료 시간 설정

## 사용 방법

### 1. JwtTokenProvider 클래스 생성

JWT 토큰을 생성하고 검증하는 핵심 클래스:

```java
package com.BO.admin.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity}") long accessTokenValidityMs,
            @Value("${jwt.refresh-token-validity}") long refreshTokenValidityMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityMs = accessTokenValidityMs;
        this.refreshTokenValidityMs = refreshTokenValidityMs;
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(String email, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityMs);

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("type", "access")
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(String email) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidityMs);

        return Jwts.builder()
                .setSubject(email)
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰에서 이메일 추출
     */
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 토큰에서 역할(Role) 추출
     */
    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    /**
     * 토큰 파싱
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = parseClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * 토큰 타입 확인 (access/refresh)
     */
    public String getTokenType(String token) {
        return parseClaims(token).get("type", String.class);
    }
}
```

### 2. JWT 토큰 응답 DTO

```java
package com.BO.admin.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;

    public static TokenResponse of(String accessToken, String refreshToken, Long expiresIn) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }
}
```

### 3. application.properties 설정

```properties
# JWT 설정
jwt.secret=your-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm-security
jwt.access-token-validity=3600000
jwt.refresh-token-validity=604800000

# Access Token: 1시간 (3600000ms)
# Refresh Token: 7일 (604800000ms)
```

### 4. Refresh Token Entity (선택사항)

Refresh Token을 DB에 저장하는 경우:

```java
package com.BO.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
```

### 5. RefreshTokenRepository (선택사항)

```java
package com.BO.admin.repository;

import com.BO.admin.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByEmail(String email);

    void deleteByEmail(String email);

    void deleteByToken(String token);
}
```

## 필요한 의존성

build.gradle에 다음 의존성이 필요합니다:

```gradle
dependencies {
    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
}
```

## 사용 예시

### 로그인 시 토큰 생성

```java
@Service
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;

    public TokenResponse login(String email, String password) {
        // 사용자 인증 로직...

        // 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(email, "USER");
        String refreshToken = jwtTokenProvider.createRefreshToken(email);

        return TokenResponse.of(
            accessToken,
            refreshToken,
            3600000L // 1시간
        );
    }
}
```

### 토큰 검증 필터

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getEmailFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            // Authentication 객체 생성 및 SecurityContext에 저장
            // ...
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

### Access Token 갱신

```java
@Service
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenResponse refreshAccessToken(String refreshToken) {
        // Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException("유효하지 않은 Refresh Token입니다.");
        }

        String tokenType = jwtTokenProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new InvalidTokenException("Refresh Token이 아닙니다.");
        }

        // 새 Access Token 발급
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        String newAccessToken = jwtTokenProvider.createAccessToken(email, "USER");

        return TokenResponse.of(newAccessToken, refreshToken, 3600000L);
    }
}
```

## 보안 고려사항

1. **Secret Key 관리**
   - 최소 256비트 길이 사용
   - 환경 변수로 관리 (프로덕션)
   - 절대 코드에 하드코딩하지 않음

2. **토큰 만료 시간**
   - Access Token: 짧게 (1시간 권장)
   - Refresh Token: 길게 (7일 권장)

3. **Refresh Token 저장**
   - DB에 저장하여 무효화 가능하도록
   - 로그아웃 시 Refresh Token 삭제

4. **HTTPS 사용**
   - 프로덕션 환경에서는 반드시 HTTPS 사용

5. **토큰 검증**
   - 서명, 만료 시간, 형식 모두 검증
   - 예외 처리 철저히

## 체크리스트

JWT 설정 시:
- [ ] build.gradle에 jjwt 의존성 추가
- [ ] application.properties에 JWT 설정 추가
- [ ] Secret Key를 256비트 이상으로 설정
- [ ] JwtTokenProvider 클래스 생성
- [ ] TokenResponse DTO 생성

Refresh Token 사용 시:
- [ ] RefreshToken Entity 생성
- [ ] RefreshTokenRepository 생성
- [ ] 로그아웃 시 Refresh Token 삭제 로직 구현
- [ ] 토큰 갱신 API 구현

보안 체크:
- [ ] Secret Key를 환경 변수로 관리
- [ ] 토큰 만료 시간 적절히 설정
- [ ] 모든 예외 상황 처리
- [ ] HTTPS 사용 (프로덕션)
