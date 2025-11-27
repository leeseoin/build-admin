---
name: spring-entity-generator
description: Spring Boot JPA Entity와 Repository를 자동으로 생성합니다. 엔티티 클래스, 테이블 매핑, JPA 어노테이션, Repository 인터페이스를 만들 때 사용하세요. User, Admin 같은 도메인 모델을 만들 때 활용합니다.
allowed-tools: Read, Write, Glob, Grep
---

# Spring Entity Generator

Spring Boot 프로젝트에서 JPA Entity와 Repository를 빠르게 생성하는 스킬입니다.

## 주요 기능

1. **Entity 클래스 자동 생성**
   - JPA 어노테이션 (@Entity, @Table, @Id, @GeneratedValue 등)
   - Lombok 어노테이션 (@Getter, @Setter, @NoArgsConstructor 등)
   - 컬럼 매핑 및 제약조건

2. **Repository 인터페이스 생성**
   - JpaRepository 상속
   - 커스텀 쿼리 메서드 추가

3. **관계 설정**
   - @OneToMany, @ManyToOne
   - @OneToOne, @ManyToMany
   - 양방향/단방향 관계 설정

## 사용 방법

### 1. 기본 Entity 생성

사용자가 "User 엔티티를 만들어줘" 또는 "회원 테이블 엔티티 생성해줘" 라고 요청하면:

```java
package com.BO.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### 2. Repository 생성

Entity 생성 후 자동으로 Repository도 생성:

```java
package com.BO.admin.repository;

import com.BO.admin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);

    // 이메일 존재 여부 확인
    boolean existsByEmail(String email);
}
```

### 3. Enum 타입 생성

필요한 경우 Enum도 생성:

```java
package com.BO.admin.entity;

public enum UserRole {
    USER,
    ADMIN
}
```

## 생성 규칙

### Entity 클래스
- **패키지**: `com.BO.admin.entity`
- **클래스명**: 도메인명 (첫 글자 대문자)
- **테이블명**: 복수형 snake_case (예: users, admin_users)
- **필수 어노테이션**:
  - `@Entity`
  - `@Table(name = "테이블명")`
  - `@Getter`, `@Setter`
  - `@NoArgsConstructor`, `@AllArgsConstructor`
  - `@Builder`

### 컬럼 규칙
- **ID**: Long 타입, @GeneratedValue(IDENTITY)
- **Timestamp**: @CreationTimestamp, @UpdateTimestamp 사용
- **제약조건**: nullable, unique, length 명시
- **Enum**: @Enumerated(EnumType.STRING) 사용

### Repository 인터페이스
- **패키지**: `com.BO.admin.repository`
- **네이밍**: {Entity명}Repository
- **상속**: JpaRepository<Entity, ID타입>
- **기본 쿼리 메서드**: findBy, existsBy 등 필요한 것만 추가

## 예시: OAuth2 사용자 엔티티

```java
@Entity
@Table(name = "oauth_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OAuthProvider provider; // GOOGLE, KAKAO

    @Column(nullable = false, unique = true)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

## 주의사항

1. **기존 파일 확인**: 같은 이름의 Entity나 Repository가 있는지 먼저 확인
2. **패키지 구조**: 프로젝트의 기본 패키지 구조를 따름
3. **의존성**: Lombok, JPA가 build.gradle에 포함되어 있는지 확인
4. **네이밍 컨벤션**:
   - Java: CamelCase
   - 테이블/컬럼: snake_case
5. **관계 설정 시**: 양방향 관계의 경우 mappedBy 설정 필수

## 체크리스트

Entity 생성 시:
- [ ] @Entity, @Table 어노테이션 추가
- [ ] Lombok 어노테이션 추가
- [ ] ID 필드 설정
- [ ] 필수 컬럼 nullable = false 설정
- [ ] Timestamp 필드 추가
- [ ] Enum 타입 별도 파일로 생성

Repository 생성 시:
- [ ] JpaRepository 상속
- [ ] @Repository 어노테이션 추가
- [ ] 필요한 커스텀 쿼리 메서드 추가
- [ ] 메서드명이 JPA 네이밍 규칙을 따르는지 확인
