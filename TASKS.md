# One Bite - 전체 TODO 관리

> 최종 업데이트: 2026-03-24

---

## MVP (Phase 1) 진행 상황

### 서버 (`server/`) — ✅ MVP 기능 완료

- [x] Split CRUD API (등록/조회/취소)
- [x] 참여(join) API + 자동 매칭 (WAITING → MATCHED)
- [x] 위치 기반 조회 (Haversine, 기본 3km)
- [x] 카카오/네이버/구글/애플 OAuth
- [x] JWT 인증 (생성/검증/필터)
- [x] 유저 프로필 API (GET/PATCH /users/me)
- [x] Docker 빌드 + PostgreSQL 운영 설정

**서버 남은 작업 (MVP 이후):**
- [x] Flyway DB 마이그레이션
- [x] 페이지네이션 (page/size 파라미터, PageResponse DTO)
- [x] PostGIS 네이티브 쿼리 전환 (프로필 기반 전략 패턴)
- [ ] Apple SignIn 서명 검증 (현재 JWT 디코딩만)
- [ ] 테스트 코드

---

### 모바일 (`mobile/`) — 🔧 MVP 진행 중

**완료:**
- [x] KMP 프로젝트 구조 (Android + iOS)
- [x] Ktor API 클라이언트 (전체 서버 API 연동)
- [x] 데이터 모델 (Auth, Split, User)
- [x] 소셜 로그인 — Android: 카카오/네이버/구글 | iOS: 애플
- [x] 토큰 저장 — Android: EncryptedSharedPrefs | iOS: Keychain
- [x] 자동 로그인
- [x] 로그인 화면 (4 OAuth + 둘러보기)
- [x] 메인 화면 (홈/지도/프로필 3탭 + FAB)
- [x] 홈 탭 (피드 리스트 + 상태 뱃지)
- [x] 상품 상세 (정보 카드 + 참여/취소)
- [x] 상품 등록 폼 (유효성 검증 포함)
- [x] Material3 테마 + 공통 UI 컴포넌트
- [x] 네비게이션 (4 라우트)

**MVP 필수 TODO:**
- [ ] 🔴 GPS 위치 캡처 — CreateSplitScreen lat/lng 0.0 하드코딩 해소
- [ ] 🔴 지도 SDK 연동 — MapTab 플레이스홀더 → 실제 지도
- [ ] 🟡 카메라/갤러리 — 상품 사진 촬영/선택 + 업로드
- [ ] 🟡 프로필 메뉴 연결 — 내 나눠사기, 참여한 나눠사기, 설정

**MVP 이후 TODO:**
- [ ] iOS OAuth SDK (카카오/네이버/구글 CocoaPods/SPM)
- [ ] OAuth 키 local.properties 분리
- [ ] iOS Base URL 분리 (현재 Android 에뮬레이터 전용)
- [ ] 에러 처리 고도화 (토큰 만료 자동 로그아웃)
- [ ] Pull-to-refresh
- [ ] 페이지네이션 (무한 스크롤)

---

### 인프라 (`infra/`) — ✅ 로컬 배포 완료

- [x] Docker Compose 개발 환경 (PostgreSQL + PostGIS + Spring Boot)
- [x] Docker Compose 운영 환경 (리소스 제한, 헬스체크)
- [x] 멀티스테이지 Dockerfile
- [x] Makefile 명령어
- [x] 환경변수 템플릿

**인프라 남은 작업:**
- [ ] CI/CD 파이프라인 (GitHub Actions)
- [ ] 리버스 프록시 (Nginx/Traefik + SSL)
- [ ] 모니터링 (Prometheus + Grafana)
- [ ] 클라우드 배포 (AWS/GCP)

---

## Phase 2 — 신뢰와 편의성

- [ ] 푸시 알림 (서버 FCM/APNs + 모바일 수신)
- [ ] 인앱 채팅
- [ ] PG 에스크로 연동 (안전거래)
- [ ] 거래 완료 인증
- [ ] 신고/차단
- [ ] 상품 카테고리 & 검색

## Phase 3 — 성장

- [ ] 단골 매장 & 정기 나눠사기
- [ ] 커뮤니티
- [ ] 통계/분석
