---
name: spring-security-config
description: Spring Security 설정을 자동으로 생성합니다. SecurityFilterChain, CORS, JWT 필터, OAuth2 설정, 권한 관리를 포함합니다. 보안 설정이 필요할 때, 인증/인가 로직을 구성할 때 사용하세요.
allowed-tools: Read, Write, Glob, Grep
---

# Spring Security Config

Spring Boot에서 Spring Security 설정을 자동으로 생성하는 스킬입니다. JWT 인증, OAuth2, CORS, 권한 관리를 포함합니다.

## 주요 기능

1. **SecurityFilterChain 설정**
   - URL별 접근 권한 설정
   - 인증/비인증 URL 관리
   - CSRF 설정

2. **JWT 필터 연동**
   - JwtAuthenticationFilter 등록
   - 토큰 검증 및 인증 처리

3. **CORS 설정**
   - 프론트엔드 연동을 위한 CORS 정책

4. **OAuth2 설정**
   - Google, Kakao 소셜 로그인
   - OAuth2UserService 구현

5. **권한 관리**
   - Role 기반 접근 제어
   - Custom 권한 체크

## 사용 방법

### 1. SecurityConfig 클래스 생성

```java
package com.BO.admin.config;

import com.BO.admin.security.jwt.JwtAuthenticationFilter;
import com.BO.admin.security.oauth.OAuth2SuccessHandler;
import com.BO.admin.security.oauth.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 사용 안 함 (JWT 사용)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // URL별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/oauth2/**",
                                "/error",
                                "/favicon.ico"
                        ).permitAll()

                        // ADMIN 권한 필요
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // USER 권한 필요
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )

                // JWT 필터 추가
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * CORS 설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173"
        ));

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * 비밀번호 암호화
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 2. JWT 인증 필터

```java
package com.BO.admin.security.jwt;

import com.BO.admin.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = resolveToken(request);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getEmailFromToken(token);
                String role = jwtTokenProvider.getRoleFromToken(token);

                // 권한 설정
                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + role)
                );

                // Authentication 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // SecurityContext에 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT 인증 성공: {}, Role: {}", email, role);
            }
        } catch (Exception e) {
            log.error("JWT 인증 실패: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Request Header에서 토큰 추출
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
```

### 3. OAuth2 User Service

```java
package com.BO.admin.security.oauth;

import com.BO.admin.entity.OAuthUser;
import com.BO.admin.entity.OAuthProvider;
import com.BO.admin.entity.UserRole;
import com.BO.admin.repository.OAuthUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuthUserRepository oauthUserRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();

        // Provider별 사용자 정보 추출
        OAuth2UserInfo userInfo = OAuth2UserInfo.of(registrationId, attributes);

        // DB에 저장 또는 업데이트
        OAuthUser user = saveOrUpdate(userInfo);

        return new CustomOAuth2User(user, attributes);
    }

    private OAuthUser saveOrUpdate(OAuth2UserInfo userInfo) {
        OAuthUser user = oauthUserRepository
                .findByProviderAndProviderId(userInfo.getProvider(), userInfo.getProviderId())
                .map(entity -> {
                    entity.setName(userInfo.getName());
                    entity.setEmail(userInfo.getEmail());
                    return entity;
                })
                .orElse(OAuthUser.builder()
                        .email(userInfo.getEmail())
                        .name(userInfo.getName())
                        .provider(userInfo.getProvider())
                        .providerId(userInfo.getProviderId())
                        .role(UserRole.USER)
                        .build());

        return oauthUserRepository.save(user);
    }
}
```

### 4. OAuth2 Success Handler

```java
package com.BO.admin.security.oauth;

import com.BO.admin.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(
                oauth2User.getEmail(),
                oauth2User.getRole()
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(
                oauth2User.getEmail()
        );

        // 프론트엔드로 리다이렉트 (토큰 포함)
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();

        log.info("OAuth2 로그인 성공: {}", oauth2User.getEmail());

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
```

### 5. OAuth2UserInfo 추상화

```java
package com.BO.admin.security.oauth;

import com.BO.admin.entity.OAuthProvider;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class OAuth2UserInfo {
    private String email;
    private String name;
    private OAuthProvider provider;
    private String providerId;

    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toUpperCase()) {
            case "GOOGLE" -> ofGoogle(attributes);
            case "KAKAO" -> ofKakao(attributes);
            default -> throw new IllegalArgumentException("지원하지 않는 OAuth Provider: " + registrationId);
        };
    }

    private static OAuth2UserInfo ofGoogle(Map<String, Object> attributes) {
        return OAuth2UserInfo.builder()
                .email((String) attributes.get("email"))
                .name((String) attributes.get("name"))
                .provider(OAuthProvider.GOOGLE)
                .providerId((String) attributes.get("sub"))
                .build();
    }

    private static OAuth2UserInfo ofKakao(Map<String, Object> attributes) {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) account.get("profile");

        return OAuth2UserInfo.builder()
                .email((String) account.get("email"))
                .name((String) profile.get("nickname"))
                .provider(OAuthProvider.KAKAO)
                .providerId(String.valueOf(attributes.get("id")))
                .build();
    }
}
```

### 6. application.properties 설정

```properties
# OAuth2 설정
spring.security.oauth2.client.registration.google.client-id=your-google-client-id
spring.security.oauth2.client.registration.google.client-secret=your-google-client-secret
spring.security.oauth2.client.registration.google.scope=profile,email

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

# 리다이렉트 URI
app.oauth2.redirect-uri=http://localhost:3000/auth/callback
```

## 필요한 의존성

build.gradle:

```gradle
dependencies {
    // Spring Security
    implementation 'org.springframework.boot:spring-boot-starter-security'

    // OAuth2
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
}
```

## URL별 권한 설정 패턴

```java
.authorizeHttpRequests(auth -> auth
    // 1. 완전 공개 (인증 불필요)
    .requestMatchers("/api/auth/**", "/api/public/**").permitAll()

    // 2. 특정 Role만 접근
    .requestMatchers("/api/admin/**").hasRole("ADMIN")

    // 3. 여러 Role 중 하나라도 있으면 접근
    .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")

    // 4. 인증만 되면 접근 (Role 무관)
    .requestMatchers("/api/protected/**").authenticated()

    // 5. 나머지는 인증 필요
    .anyRequest().authenticated()
)
```

## 현재 인증된 사용자 정보 가져오기

```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<UserInfo> getCurrentUser(
            @AuthenticationPrincipal String email
    ) {
        // email = SecurityContext에서 추출된 사용자 이메일
        return ResponseEntity.ok(userService.getUserInfo(email));
    }
}
```

## CORS 설정 커스터마이징

프로덕션 환경별로 다르게 설정:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource(
        @Value("${app.cors.allowed-origins}") List<String> allowedOrigins
) {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(allowedOrigins);
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

application.properties:

```properties
# 개발 환경
app.cors.allowed-origins=http://localhost:3000,http://localhost:5173

# 프로덕션 환경
# app.cors.allowed-origins=https://yourdomain.com
```

## 체크리스트

Security 설정:
- [ ] build.gradle에 Spring Security 의존성 추가
- [ ] SecurityConfig 클래스 생성
- [ ] SecurityFilterChain Bean 등록
- [ ] PasswordEncoder Bean 등록
- [ ] CORS 설정
- [ ] URL별 권한 설정

JWT 연동:
- [ ] JwtAuthenticationFilter 생성
- [ ] JWT 필터를 SecurityFilterChain에 등록
- [ ] 토큰 추출 로직 구현
- [ ] SecurityContext에 인증 정보 저장

OAuth2 설정 (선택):
- [ ] CustomOAuth2UserService 구현
- [ ] OAuth2SuccessHandler 구현
- [ ] OAuth2UserInfo 추상화
- [ ] application.properties에 OAuth2 설정 추가
- [ ] Google/Kakao Client ID/Secret 설정

테스트:
- [ ] 인증 없이 접근 가능한 URL 테스트
- [ ] JWT 토큰으로 보호된 URL 테스트
- [ ] Role별 권한 테스트
- [ ] OAuth2 로그인 플로우 테스트
- [ ] CORS 정책 테스트
