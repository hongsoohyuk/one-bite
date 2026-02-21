# One Bite API 명세

> 서버-클라이언트 간 계약. 이 문서를 기준으로 서버/클라이언트가 독립 개발 가능.

Base URL: `http://localhost:8080/api`

## 인증

모든 인증 필요 요청에 헤더 포함:
```
Authorization: Bearer <jwt-token>
```

---

## Auth API

### POST /auth/kakao
카카오 소셜 로그인. 인가코드를 보내면 JWT 반환.

**Request:**
```json
{
  "code": "카카오_인가코드"
}
```

**Response: 200**
```json
{
  "token": "eyJhbGciOi...",
  "userId": 1,
  "nickname": "한입유저",
  "isNewUser": true
}
```

### POST /auth/naver
네이버 소셜 로그인. 인가코드와 state를 보내면 JWT 반환.

**Request:**
```json
{
  "code": "네이버_인가코드",
  "state": "csrf_state_value"
}
```

**Response: 200**
```json
{
  "token": "eyJhbGciOi...",
  "userId": 1,
  "nickname": "한입유저",
  "isNewUser": true
}
```

### POST /auth/google
Google 소셜 로그인. 인가코드를 보내면 JWT 반환.

**Request:**
```json
{
  "code": "google_인가코드"
}
```

**Response: 200**
```json
{
  "token": "eyJhbGciOi...",
  "userId": 1,
  "nickname": "한입유저",
  "isNewUser": true
}
```

### POST /auth/apple
Apple 소셜 로그인. 클라이언트에서 받은 Apple ID 토큰을 보내면 JWT 반환.

**Request:**
```json
{
  "idToken": "apple_id_token_jwt"
}
```

**Response: 200**
```json
{
  "token": "eyJhbGciOi...",
  "userId": 1,
  "nickname": "한입유저",
  "isNewUser": true
}
```

---

## Split API (인증 필요)

### GET /splits
나눠사기 목록 조회.

**Query Params:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| status | String | N | WAITING, MATCHED, COMPLETED, CANCELLED |
| lat | Double | N | 기준 위도 (위치 기반 조회 시) |
| lng | Double | N | 기준 경도 |
| radiusKm | Double | N | 반경 km (기본 3.0) |

**Response: 200**
```json
[
  {
    "id": 1,
    "productName": "두쫀쿠",
    "totalPrice": 20000,
    "totalQty": 4,
    "splitCount": 2,
    "pricePerPerson": 10000,
    "qtyPerPerson": 2,
    "imageUrl": "https://...",
    "latitude": 37.5665,
    "longitude": 126.9780,
    "address": "서울 중구 세종대로",
    "status": "WAITING",
    "createdAt": "2025-02-21T15:00:00",
    "author": {
      "id": 1,
      "nickname": "한입유저",
      "profileImageUrl": null
    }
  }
]
```

### GET /splits/{id}
단건 조회.

### POST /splits
나눠사기 등록.

**Request:**
```json
{
  "productName": "두쫀쿠",
  "totalPrice": 20000,
  "totalQty": 4,
  "splitCount": 2,
  "imageUrl": "https://...",
  "latitude": 37.5665,
  "longitude": 126.9780,
  "address": "서울 중구 세종대로"
}
```

**Response: 201** (Split 객체)

### PATCH /splits/{id}/cancel
나눠사기 취소. 작성자만 가능.

**Response: 200** (업데이트된 Split 객체)

### POST /splits/{id}/join
나눠사기 참여 요청.

**Response: 200**
```json
{
  "splitId": 1,
  "status": "MATCHED",
  "partnerName": "김철수"
}
```

---

## User API (인증 필요)

### GET /users/me
내 프로필 조회.

**Response: 200**
```json
{
  "id": 1,
  "nickname": "한입유저",
  "profileImageUrl": null,
  "createdAt": "2025-02-21T15:00:00"
}
```

### PATCH /users/me
프로필 수정.

**Request:**
```json
{
  "nickname": "새닉네임"
}
```

---

## 에러 응답 (공통)

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "상품명은 필수입니다"
}
```

| 코드 | 상황 |
|------|------|
| 400 | 잘못된 요청 (유효성 검증 실패) |
| 401 | 인증 필요 (토큰 없음/만료) |
| 403 | 권한 없음 (남의 글 취소 등) |
| 404 | 리소스 없음 |
