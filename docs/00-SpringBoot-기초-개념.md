# Spring Boot 기초 개념

Spring Boot를 처음 배우는 사람을 위한 핵심 개념 설명입니다.

---

## 1. Spring Boot란?

**Spring Boot**는 Java 기반 웹 애플리케이션을 빠르고 쉽게 만들 수 있게 해주는 프레임워크입니다.

### 주요 특징
- **자동 설정**: 복잡한 설정을 자동으로 처리
- **내장 서버**: Tomcat 같은 서버가 이미 내장되어 있음
- **쉬운 시작**: `@SpringBootApplication` 하나로 시작 가능

---

## 2. 핵심 개념

### 2.1 Annotation (어노테이션)
Java 코드에 메타데이터를 추가하는 방법입니다. `@` 기호로 시작합니다.

```java
@RestController  // 이 클래스는 REST API 컨트롤러다
public class UserController {
    // ...
}
```

**자주 쓰는 어노테이션**
- `@SpringBootApplication`: 메인 애플리케이션 클래스
- `@RestController`: REST API 컨트롤러
- `@Service`: 비즈니스 로직 서비스
- `@Repository`: 데이터베이스 접근 계층
- `@Entity`: 데이터베이스 테이블과 매핑되는 클래스

---

### 2.2 의존성 주입 (Dependency Injection, DI)

Spring의 핵심 기능입니다. 객체를 직접 만들지 않고 Spring이 자동으로 만들어서 주입해줍니다.

```java
@Service
public class UserService {

    private final UserRepository userRepository;

    // 생성자를 통해 UserRepository를 자동으로 주입받음
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

**Lombok의 `@RequiredArgsConstructor`를 사용하면 더 간단해집니다:**

```java
@Service
@RequiredArgsConstructor  // final 필드에 대한 생성자 자동 생성
public class UserService {
    private final UserRepository userRepository;
    // 생성자 코드 불필요!
}
```

---

### 2.3 계층 구조 (Layered Architecture)

Spring Boot 애플리케이션은 보통 3~4개의 계층으로 나뉩니다:

```
┌─────────────────────┐
│   Controller        │  ← HTTP 요청을 받는 곳
├─────────────────────┤
│   Service           │  ← 비즈니스 로직
├─────────────────────┤
│   Repository        │  ← 데이터베이스 접근
├─────────────────────┤
│   Database          │
└─────────────────────┘
```

#### Controller (컨트롤러)
- 사용자의 HTTP 요청을 받음
- Service를 호출
- 결과를 JSON으로 반환

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<User> signup(@RequestBody SignupRequest request) {
        User user = userService.signup(request);
        return ResponseEntity.ok(user);
    }
}
```

#### Service (서비스)
- 비즈니스 로직을 처리
- Repository를 호출
- 트랜잭션 관리

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User signup(SignupRequest request) {
        // 비즈니스 로직: 중복 체크, 비밀번호 해싱 등
        User user = User.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        return userRepository.save(user);
    }
}
```

#### Repository (레포지토리)
- 데이터베이스와 직접 통신
- JPA를 사용하면 SQL 작성 불필요

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);
    // JPA가 자동으로 SQL 생성: SELECT * FROM tb_user WHERE login_id = ?
}
```

---

### 2.4 Entity (엔티티)

데이터베이스 테이블과 1:1로 매핑되는 Java 클래스입니다.

```java
@Entity
@Table(name = "tb_user")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id  // Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto Increment
    private Long userId;

    @Column(nullable = false)  // NOT NULL
    private String loginId;

    private String password;
}
```

**Lombok 어노테이션 설명:**
- `@Getter`: 모든 필드에 대한 getter 메서드 자동 생성
- `@NoArgsConstructor`: 파라미터 없는 기본 생성자
- `@AllArgsConstructor`: 모든 필드를 파라미터로 받는 생성자
- `@Builder`: 빌더 패턴 사용 가능

---

### 2.5 DTO (Data Transfer Object)

계층 간 데이터 전송을 위한 객체입니다. Entity와 분리하여 사용합니다.

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    private String loginId;
    private String password;
    private String userName;
}
```

**왜 Entity와 DTO를 분리하나요?**
- Entity는 데이터베이스와 밀접하게 연결됨
- DTO는 API 요청/응답에 특화됨
- 분리하면 유지보수가 쉬워짐

---

## 3. Spring Security 기초

### 3.1 Spring Security란?

애플리케이션의 **인증(Authentication)** 과 **인가(Authorization)** 를 담당하는 프레임워크입니다.

- **인증**: 사용자가 누구인지 확인 (로그인)
- **인가**: 사용자가 무엇을 할 수 있는지 확인 (권한)

### 3.2 JWT (JSON Web Token)

토큰 기반 인증 방식입니다.

**동작 방식:**
1. 사용자가 로그인
2. 서버가 JWT 토큰 발급
3. 클라이언트는 매 요청마다 토큰을 헤더에 포함
4. 서버는 토큰을 검증하여 사용자 확인

**JWT 구조:**
```
Header.Payload.Signature
```

**예시:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

---

## 4. JPA (Java Persistence API)

### 4.1 JPA란?

Java에서 관계형 데이터베이스를 객체처럼 다룰 수 있게 해주는 기술입니다.

**SQL 없이 데이터베이스 조작:**
```java
// SQL: INSERT INTO tb_user VALUES (...)
User user = new User();
userRepository.save(user);

// SQL: SELECT * FROM tb_user WHERE user_id = 1
User user = userRepository.findById(1L).orElseThrow();

// SQL: UPDATE tb_user SET user_name = '홍길동' WHERE user_id = 1
user.setUserName("홍길동");
userRepository.save(user);

// SQL: DELETE FROM tb_user WHERE user_id = 1
userRepository.deleteById(1L);
```

### 4.2 쿼리 메서드

메서드 이름만으로 SQL을 자동 생성합니다.

```java
public interface UserRepository extends JpaRepository<User, Long> {

    // SELECT * FROM tb_user WHERE login_id = ?
    Optional<User> findByLoginId(String loginId);

    // SELECT * FROM tb_user WHERE user_name = ? AND social_login_yn = ?
    List<User> findByUserNameAndSocialLoginYn(String userName, Boolean socialLoginYn);

    // SELECT COUNT(*) FROM tb_user WHERE login_id = ?
    boolean existsByLoginId(String loginId);
}
```

---

## 5. OAuth2 소셜 로그인

### 5.1 OAuth2란?

사용자가 다른 서비스(Google, Kakao 등)의 계정으로 로그인할 수 있게 해주는 표준 프로토콜입니다.

**흐름:**
```
1. 사용자 → 우리 앱: "Google로 로그인하기" 클릭
2. 우리 앱 → Google: 로그인 페이지로 리다이렉트
3. 사용자 → Google: 로그인 및 권한 허용
4. Google → 우리 앱: 인증 코드 전달
5. 우리 앱 → Google: 인증 코드로 액세스 토큰 요청
6. Google → 우리 앱: 액세스 토큰 전달
7. 우리 앱 → Google: 액세스 토큰으로 사용자 정보 요청
8. Google → 우리 앱: 사용자 정보 전달 (이메일, 이름 등)
9. 우리 앱: 자동 회원가입 또는 로그인 처리
```

### 5.2 Spring Boot에서 OAuth2

Spring Boot는 OAuth2를 자동으로 처리해줍니다.

```properties
# application.properties
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_SECRET
```

이것만 설정하면 `/oauth2/authorization/google` 엔드포인트가 자동 생성됩니다!

---

## 6. 자주 쓰는 어노테이션 정리

### Controller
- `@RestController`: REST API 컨트롤러
- `@RequestMapping("/api/users")`: 기본 경로 설정
- `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`: HTTP 메서드
- `@RequestBody`: JSON 요청 본문을 Java 객체로 변환
- `@PathVariable`: URL 경로에서 값 추출
- `@RequestParam`: 쿼리 파라미터 추출

### Service
- `@Service`: 서비스 클래스
- `@Transactional`: 트랜잭션 관리 (성공 시 커밋, 실패 시 롤백)

### Repository
- `@Repository`: 레포지토리 클래스

### Entity
- `@Entity`: JPA 엔티티
- `@Table(name = "tb_user")`: 테이블 이름 지정
- `@Id`: Primary Key
- `@GeneratedValue`: Auto Increment
- `@Column`: 컬럼 설정

### Security
- `@PreAuthorize("hasRole('ADMIN')")`: 관리자만 접근 가능
- `@EnableWebSecurity`: Security 활성화

### Validation
- `@Valid`: DTO 검증 활성화
- `@NotBlank`: 빈 문자열 불가
- `@Email`: 이메일 형식 검증
- `@Size(min=8)`: 최소 길이

---

## 7. 프로젝트 실행 방법

### 7.1 IDE에서 실행
1. IntelliJ IDEA 또는 Eclipse에서 프로젝트 열기
2. `AdminApplication.java` 파일 찾기
3. `main` 메서드 옆의 실행 버튼 클릭

### 7.2 터미널에서 실행
```bash
# Gradle 사용
./gradlew bootRun

# 또는 JAR 빌드 후 실행
./gradlew build
java -jar build/libs/admin-0.0.1-SNAPSHOT.jar
```

### 7.3 접속
브라우저에서 `http://localhost:8080` 접속

---

## 8. 디버깅 팁

### 로그 확인
```java
@Slf4j  // Lombok의 로깅 어노테이션
@Service
public class UserService {

    public void someMethod() {
        log.debug("디버그 로그");
        log.info("정보 로그");
        log.warn("경고 로그");
        log.error("에러 로그");
    }
}
```

### SQL 확인
`application.properties`에 추가:
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### 에러 메시지 읽기
에러가 나면 스택 트레이스(Stack Trace)를 확인하세요. 맨 위가 가장 최근 에러입니다.

---

## 다음 단계

이 기초 개념을 이해했다면, `04-구현-TODO-리스트.md`를 보고 단계별로 구현을 시작하세요!

**추천 학습 자료:**
- Spring Boot 공식 문서: https://spring.io/projects/spring-boot
- Baeldung Spring 튜토리얼: https://www.baeldung.com/spring-boot
- JPA 가이드: https://spring.io/guides/gs/accessing-data-jpa/
