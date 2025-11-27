# API 엔드포인트 설계

## 기본 URL
- 로컬 개발: `http://localhost:8080`
- 프로덕션: `https://your-domain.com`

## 공통 응답 형식

### 성공 응답
```json
{
  "success": true,
  "data": { ... },
  "message": "Success"
}
```

### 실패 응답
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "에러 메시지"
  },
  "timestamp": "2024-01-20T10:30:00"
}
```

---

## 1. 사용자 인증 API

### 1.1 회원가입
**POST** `/api/auth/signup`

**요청**
```json
{
  "loginId": "user@example.com",
  "password": "password123!",
  "userName": "홍길동",
  "language": "KOR"  // 선택사항, 기본값 "ENG"
}
```

**응답** (201 Created)
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "loginId": "user@example.com",
    "userName": "홍길동",
    "registeredPath": "EMAIL"
  },
  "message": "회원가입이 완료되었습니다."
}
```

**에러**
- `409 CONFLICT`: 이미 존재하는 이메일
- `400 BAD_REQUEST`: 입력값 검증 실패

---

### 1.2 로그인
**POST** `/api/auth/login`

**요청**
```json
{
  "loginId": "user@example.com",
  "password": "password123!"
}
```

**응답** (200 OK)
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "userId": 1,
    "expiresIn": 3600
  },
  "message": "로그인 성공"
}
```

**에러**
- `401 UNAUTHORIZED`: 잘못된 이메일 또는 비밀번호
- `404 NOT_FOUND`: 존재하지 않는 사용자

---

### 1.3 토큰 갱신
**POST** `/api/auth/refresh`

**요청**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**응답** (200 OK)
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  },
  "message": "토큰 갱신 성공"
}
```

**에러**
- `401 UNAUTHORIZED`: 유효하지 않은 Refresh Token
- `404 NOT_FOUND`: DB에 존재하지 않는 Refresh Token

---

### 1.4 로그아웃
**POST** `/api/auth/logout`

**헤더**
```
Authorization: Bearer {accessToken}
```

**요청**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**응답** (200 OK)
```json
{
  "success": true,
  "message": "로그아웃 성공"
}
```

---

## 2. 소셜 로그인 API

### 2.1 Google 로그인
**GET** `/oauth2/authorization/google`

사용자를 Google 로그인 페이지로 리다이렉트합니다.

**리다이렉트 후 콜백**
`/login/oauth2/code/google`

최종적으로 다음 형식으로 토큰을 반환합니다:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "userId": 1
}
```

---

### 2.2 Kakao 로그인
**GET** `/oauth2/authorization/kakao`

사용자를 Kakao 로그인 페이지로 리다이렉트합니다.

**리다이렉트 후 콜백**
`/login/oauth2/code/kakao`

최종적으로 다음 형식으로 토큰을 반환합니다:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "userId": 1
}
```

---

## 3. 관리자 인증 API

### 3.1 관리자 로그인
**POST** `/api/admin/auth/login`

**요청**
```json
{
  "loginId": "admin@example.com",
  "password": "adminPassword123!"
}
```

**응답** (200 OK)
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "adminId": 1,
    "level": "ADMIN",
    "expiresIn": 3600
  },
  "message": "관리자 로그인 성공"
}
```

**에러**
- `401 UNAUTHORIZED`: 잘못된 이메일 또는 비밀번호
- `403 FORBIDDEN`: 삭제된 관리자 계정

---

### 3.2 관리자 토큰 갱신
**POST** `/api/admin/auth/refresh`

사용자 토큰 갱신과 동일하지만, `userType`이 `ADMIN`입니다.

---

### 3.3 관리자 로그아웃
**POST** `/api/admin/auth/logout`

**헤더**
```
Authorization: Bearer {accessToken}
```

**요청**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**응답** (200 OK)
```json
{
  "success": true,
  "message": "관리자 로그아웃 성공"
}
```

---

## 4. 사용자 정보 API (인증 필요)

### 4.1 내 정보 조회
**GET** `/api/user/me`

**헤더**
```
Authorization: Bearer {accessToken}
```

**응답** (200 OK)
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "loginId": "user@example.com",
    "userName": "홍길동",
    "socialLoginYn": false,
    "registeredPath": "EMAIL",
    "lastLoginAt": "2024-01-20T10:30:00",
    "language": "KOR"
  }
}
```

---

### 4.2 내 정보 수정
**PATCH** `/api/user/me`

**헤더**
```
Authorization: Bearer {accessToken}
```

**요청**
```json
{
  "userName": "김철수",
  "language": "ENG"
}
```

**응답** (200 OK)
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "userName": "김철수",
    "language": "ENG"
  },
  "message": "정보가 수정되었습니다."
}
```

---

## 5. 관리자 전용 API (관리자 인증 필요)

### 5.1 사용자 목록 조회
**GET** `/api/admin/users`

**헤더**
```
Authorization: Bearer {accessToken}
```

**쿼리 파라미터**
- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 20)
- `sort`: 정렬 (예: `registeredAt,desc`)

**응답** (200 OK)
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "userId": 1,
        "loginId": "user@example.com",
        "userName": "홍길동",
        "registeredAt": "2024-01-20T10:30:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "currentPage": 0
  }
}
```

---

## 에러 코드 정의

| HTTP Status | Error Code | 설명 |
|------------|-----------|------|
| 400 | INVALID_INPUT | 입력값 검증 실패 |
| 401 | INVALID_CREDENTIALS | 잘못된 인증 정보 |
| 401 | TOKEN_EXPIRED | 토큰 만료 |
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 403 | ACCESS_DENIED | 접근 권한 없음 |
| 404 | USER_NOT_FOUND | 사용자를 찾을 수 없음 |
| 409 | DUPLICATE_EMAIL | 이미 존재하는 이메일 |
| 500 | INTERNAL_SERVER_ERROR | 서버 내부 오류 |

---

## 테스트 시나리오

### 시나리오 1: 일반 회원가입 및 로그인
1. POST `/api/auth/signup` - 회원가입
2. POST `/api/auth/login` - 로그인하여 토큰 발급
3. GET `/api/user/me` - Access Token으로 내 정보 조회
4. POST `/api/auth/refresh` - Refresh Token으로 토큰 갱신
5. POST `/api/auth/logout` - 로그아웃

### 시나리오 2: 소셜 로그인
1. GET `/oauth2/authorization/google` - Google 로그인
2. 자동으로 회원가입 또는 로그인
3. 토큰 발급
4. GET `/api/user/me` - 내 정보 조회

### 시나리오 3: 관리자 로그인
1. POST `/api/admin/auth/login` - 관리자 로그인
2. GET `/api/admin/users` - 사용자 목록 조회 (관리자 권한 필요)
