# Server - Spring Boot 백엔드

## 기술 스택

| 항목 | 기술 | 비고 |
|------|------|------|
| 언어 | Kotlin 1.9.25 | |
| 프레임워크 | Spring Boot 3.5.0 | |
| DB (개발) | H2 in-memory | `jdbc:h2:mem:onebite` |
| DB (운영) | PostgreSQL 16 + PostGIS 3.4 | 위치 기반 쿼리 |
| 인증 | JWT (JJWT 0.12.6) + 소셜 OAuth | |
| Java | JDK 17 | |

## 실행 방법

```bash
# 개발 (H2)
./gradlew bootRun
# http://localhost:8080
# H2 콘솔: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:onebite)

# Docker (PostgreSQL)
cd .. && make dev
```

## 프로젝트 구조

```
src/main/kotlin/com/onebite/server/
├── OneBiteServerApplication.kt
├── auth/                    # 인증 도메인
│   ├── AuthController.kt   # POST /api/auth/{kakao,naver,google,apple}
│   ├── AuthService.kt      # OAuth 플로우 오케스트레이션
│   ├── JwtProvider.kt      # JWT 생성/검증 (HMAC-SHA256)
│   ├── JwtFilter.kt        # Bearer 토큰 추출 → SecurityContext
│   ├── SecurityConfig.kt   # /api/auth/** 퍼블릭, 나머지 인증 필요
│   ├── KakaoClient.kt      # 카카오 token+userInfo
│   ├── NaverClient.kt      # 네이버 token+userInfo
│   ├── GoogleClient.kt     # 구글 token+userInfo
│   ├── AppleClient.kt      # 애플 idToken 디코딩
│   └── SocialUserInfo.kt   # 통합 DTO (id, nickname, profileImageUrl)
├── split/                   # 나눠사기 도메인
│   ├── SplitController.kt  # CRUD + join + cancel + nearby
│   ├── SplitService.kt     # 비즈니스 로직
│   ├── SplitDto.kt         # CreateSplitDto, SplitResponse, AuthorDto, ParticipantDto
│   ├── SplitRequest.kt     # Entity (상품 정보 + 위치 + 상태)
│   ├── SplitParticipant.kt # Entity (참여자)
│   ├── SplitRepository.kt  # findNearby (Haversine), findByStatus, findByAuthorId
│   └── SplitParticipantRepository.kt
└── user/                    # 유저 도메인
    ├── User.kt              # Entity (provider, providerId, nickname, profileImageUrl)
    ├── UserRepository.kt    # findByProviderAndProviderId
    ├── UserController.kt    # GET/PATCH /api/users/me
    ├── UserService.kt       # 프로필 조회/수정
    └── UserDto.kt           # UserResponse, UpdateUserDto
```

## API 엔드포인트

| Method | Path | Auth | 설명 |
|--------|------|------|------|
| POST | /api/auth/kakao | X | 카카오 로그인 (code) |
| POST | /api/auth/naver | X | 네이버 로그인 (code + state) |
| POST | /api/auth/google | X | 구글 로그인 (code) |
| POST | /api/auth/apple | X | 애플 로그인 (idToken) |
| GET | /api/splits | O | 목록 (status, lat/lng/radiusKm 필터) |
| GET | /api/splits/{id} | O | 단건 조회 |
| POST | /api/splits | O | 등록 |
| GET | /api/splits/my | O | 내가 만든 나눠사기 |
| POST | /api/splits/{id}/join | O | 참여 |
| PATCH | /api/splits/{id}/cancel | O | 취소 (작성자만, WAITING만) |
| GET | /api/users/me | O | 내 프로필 |
| PATCH | /api/users/me | O | 프로필 수정 (nickname) |

## 핵심 비즈니스 로직

- **참여(join)**: WAITING 상태만 가능, 본인 글 불가, 중복 참여 불가, 참여자 충족 시 → MATCHED 자동 전환
- **취소(cancel)**: 작성자만, WAITING 상태만 → CANCELLED
- **위치 조회(nearby)**: Haversine 공식으로 거리 계산, 기본 반경 3km, 거리순 정렬

## 설정 프로필

- **기본 (H2)**: `application.properties` — Flyway + DDL validate, SQL 로깅 ON
- **운영 (PostgreSQL)**: `application-prod.properties` — Flyway (공통+prod 마이그레이션), 환경변수로 설정 주입

## 구현 상태

### 완료
- [x] Split CRUD API (등록/조회/취소)
- [x] 참여(join) API + 자동 매칭
- [x] 위치 기반 조회 (H2: Haversine, PostgreSQL: PostGIS ST_DWithin)
- [x] 카카오/네이버/구글/애플 OAuth
- [x] JWT 인증 (생성/검증/필터)
- [x] 유저 프로필 API (GET/PATCH)
- [x] Docker 멀티스테이지 빌드
- [x] PostgreSQL + PostGIS 운영 설정
- [x] Flyway DB 마이그레이션 (V1: 초기 스키마, V2: PostGIS geography)
- [x] 페이지네이션 (목록 API — page/size 파라미터, PageResponse DTO)
- [x] PostGIS 네이티브 쿼리 (프로필 기반 전략 — SplitLocationQuery)

### TODO (우선순위순)

**즉시 (배포 블로커)**
- [ ] EC2 배포 — Docker Compose로 PostgreSQL + Spring Boot 올리기
- [ ] 둘러보기(비인증) 허용 — GET /api/splits 비인증으로 열기 (SecurityConfig 수정)
- [ ] OAuth redirect relay — iOS 웹 OAuth용 엔드포인트 (`GET /api/auth/callback/{provider}` → 커스텀 스킴 리다이렉트)

**다음 (안정성)**
- [ ] Apple SignIn 서명 검증 (현재 JWT 디코딩만, Apple 공개키 검증 누락)
- [ ] 테스트 코드 (현재 contextLoads 스모크 테스트만)
- [ ] Rate limiting
- [ ] Swagger/OpenAPI 문서 자동 생성

**Phase 2**
- [ ] 푸시 알림 (FCM + APNs)
- [ ] 인앱 채팅
- [ ] PG 에스크로 연동
