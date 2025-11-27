# Admin Backend - 인증 시스템

Spring Boot 기반의 일반 로그인 및 소셜 로그인(OAuth2)을 지원하는 백엔드 프로젝트입니다.

## 프로젝트 개요

- **기술 스택**: Spring Boot 3.5.7, Java 21, MySQL, JWT, OAuth2
- **주요 기능**:
  - 일반 로그인 (이메일/비밀번호)
  - 소셜 로그인 (Google, Kakao)
  - JWT 기반 인증 (Access Token + Refresh Token)
  - 사용자/관리자 분리

## 문서 구조

프로젝트를 이해하고 구현하기 위한 모든 문서는 `docs/` 폴더에 있습니다.

### 📚 문서 읽는 순서

1. **[00-SpringBoot-기초-개념.md](docs/00-SpringBoot-기초-개념.md)**
   - Spring Boot가 처음이신가요? 여기서 시작하세요!
   - 핵심 개념과 용어 설명
   - 계층 구조, 어노테이션, JPA, Security 기초

2. **[01-프로젝트-개요.md](docs/01-프로젝트-개요.md)**
   - 프로젝트 목표 및 요구사항
   - 기술 스택 상세
   - 주요 기능 목록

3. **[02-아키텍처-설계.md](docs/02-아키텍처-설계.md)**
   - 전체 시스템 아키텍처
   - 패키지 구조
   - 인증 흐름도
   - 계층별 역할 설명

4. **[03-API-엔드포인트-설계.md](docs/03-API-엔드포인트-설계.md)**
   - 모든 API 엔드포인트 명세
   - 요청/응답 예시
   - 에러 코드 정의
   - 테스트 시나리오

5. **[04-구현-TODO-리스트.md](docs/04-구현-TODO-리스트.md)**
   - 단계별 구현 가이드
   - Phase별 체크리스트
   - 구현 순서 및 전략

## 빠른 시작

### 1. 문서 확인
위의 순서대로 문서를 읽으면서 프로젝트를 이해하세요.

### 2. 개발 환경 준비
- Java 21 설치
- MySQL 설치 및 실행
- IntelliJ IDEA 또는 Eclipse 설치

### 3. 데이터베이스 생성
```sql
CREATE DATABASE admin_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 4. 구현 시작
`docs/04-구현-TODO-리스트.md`의 Phase 1부터 시작하세요!

## 프로젝트 구조

```
admin/
├── docs/                        # 📚 모든 설계 문서
│   ├── 00-SpringBoot-기초-개념.md
│   ├── 01-프로젝트-개요.md
│   ├── 02-아키텍처-설계.md
│   ├── 03-API-엔드포인트-설계.md
│   └── 04-구현-TODO-리스트.md
│
├── sql/                         # SQL 스크립트
│   └── Admin-Page-Table.sql     # DB 테이블 정의
│
├── src/
│   ├── main/
│   │   ├── java/com/BO/admin/
│   │   │   ├── AdminApplication.java
│   │   │   ├── config/          # 설정 클래스 (구현 예정)
│   │   │   ├── entity/          # 엔티티 (구현 예정)
│   │   │   ├── repository/      # 레포지토리 (구현 예정)
│   │   │   ├── service/         # 서비스 (구현 예정)
│   │   │   ├── controller/      # 컨트롤러 (구현 예정)
│   │   │   ├── security/        # 보안 관련 (구현 예정)
│   │   │   ├── dto/             # DTO (구현 예정)
│   │   │   └── exception/       # 예외 처리 (구현 예정)
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/                    # 테스트 코드 (구현 예정)
│
├── build.gradle                 # Gradle 의존성 설정
└── README.md                    # 이 파일
```

## 다음 단계

1. **학습하기**: `docs/00-SpringBoot-기초-개념.md`부터 읽기
2. **계획 세우기**: `docs/04-구현-TODO-리스트.md` 확인
3. **구현 시작**: Phase 1부터 단계별로 진행
4. **테스트**: 각 Phase마다 동작 확인

## 도움이 필요하신가요?

- 각 문서에 상세한 설명과 코드 예시가 있습니다
- TODO 리스트를 체크하면서 천천히 진행하세요
- 막히는 부분이 있으면 해당 Phase로 돌아가서 다시 확인하세요

---

**화이팅! 🚀**
