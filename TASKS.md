# Nthing (엔띵) - 전체 TODO 관리

> 최종 업데이트: 2026-06-15 (실제 코드 상태 기준으로 동기화)

영역별 상세는 `CLAUDE.md`(전체), `server/CLAUDE.md`, `infra/CLAUDE.md`,
모바일 마이그레이션은 `docs/superpowers/specs/2026-05-18-client-rewrite-design.md` 참조.

---

## Phase 1 (MVP) — ✅ 기능 + 배포 완료

남은 건 **실기기 풀 플로우 검증**(하드웨어 필요)뿐.

### 서버 (`server/`) — ✅ 완료

- [x] Split CRUD + 참여(join) + 취소(cancel) + 자동 매칭 (WAITING → MATCHED)
- [x] 위치 기반 조회 (H2: Haversine, PostgreSQL: PostGIS 전략 패턴)
- [x] 소셜 로그인 4종 (카카오/네이버/구글/애플) + JWT
- [x] **Apple idToken 서명 검증** (JWKS RSA + iss/aud, kid 캐시·로테이션 — 단순 디코딩 아님)
- [x] **OAuth 웹 릴레이** `GET/POST /api/auth/callback/{provider}` → 커스텀 스킴(`nthing://auth/callback`)
- [x] 유저 프로필 (GET/PATCH /users/me) + 페이지네이션 + Flyway
- [x] S3 presigned URL 업로드 (POST /uploads/sign)
- [x] 내가 등록/참여한 나눠사기 (GET /splits/my, /splits/participated)
- [x] docs/api-spec.md 최신 / EC2 + PostgreSQL 배포
- [x] GET /api/splits 비인증 둘러보기 허용

### 모바일 (`mobile/`) — ✅ Vite + React + Capacitor 마이그레이션 완료

> 기존 KMP 코드는 `mobile-kmp/` 아카이브(후속 삭제 예정).

- [x] Vite scaffold + Capacitor 8 셸 (iOS/Android) + Tailwind 디자인 토큰 + 디자인 시스템 8종
- [x] API 클라이언트(auth/me + splits/uploads) + authStore/locationStore + TanStack Query 훅
- [x] 7화면 이식: Login + MainLayout(AppBar/BottomNav/FAB) + Home/Profile/Create/Detail/List
- [x] OAuth 4종 서버 릴레이(`nthing://`) 배선 + dev-login
- [x] 카카오맵 JS SDK (등록 화면 위치 선택 `LocationPicker`)
- [x] Capacitor Plugins: Preferences/Browser/App/Camera/Geolocation/Push
- [x] iOS/Android 디버그 빌드 검증 (2026-05-29)
- [x] i18n(4개 로케일) + 다크모드 테마
- [x] BASE_URL dev/prod 분리 (`.env.development`=`/api` proxy, `.env.production`=`https://api.nthing.app/api`)

### 인프라 (`infra/`) — ✅ 배포 + CI/CD + 도메인 + HTTPS 완료

- [x] Docker Compose(개발/운영) + 멀티스테이지 Dockerfile + HEALTHCHECK
- [x] AWS Terraform (EC2 t4g.small + EIP + S3 + IAM) + GitHub Actions 자동 배포(GHCR + SSM)
- [x] **도메인 확보 + HTTPS** (2026-06-02): `api.nthing.app`(EC2 nginx + Let's Encrypt), 랜딩 `nthing.app`(Vercel)
- [x] **OAuth 4종 실값 교체** — provider 콘솔 redirect `https://api.nthing.app/api/auth/callback/{provider}` 등록 완료

---

## MVP 남은 것 (코드 아님 — 운영/하드웨어)

- [ ] **실기기 E2E 스모크** — iPhone 17 + Apple Developer 승인됨(Team `5WQ7PQ4YN4`). 코어 앱 + 로그인 4종 + API 실기기 테스트. iOS 카카오맵 도메인·APNs 푸시는 추가 콘솔 설정 후

---

## 안정성/하드닝 (코드 가능, 미착수)

**서버**
- [ ] 테스트 코드 확충
- [ ] Rate limiting (라이브 `api.nthing.app` 인증/쓰기 엔드포인트 보호)
- [ ] Swagger/OpenAPI 자동 생성

**인프라**
- [ ] 모니터링 (CloudWatch 또는 Prometheus/Grafana) + 로그 집계
- [ ] PostgreSQL 자동 백업
- [ ] `deploy.yml` EC2 인스턴스 ID 하드코딩 제거 (태그 기반 조회)
- [ ] AMI drift 방지 (`lifecycle.ignore_changes = [ami]` 또는 AMI 핀)

---

## Phase 2 — 신뢰와 편의성 (대부분 완료)

- [~] **푸시 알림** — 서버 FCM 단일 채널 + 모바일 수신 코드 완성. `FcmConfig` 가
  `FIREBASE_CREDENTIALS_BASE64`(우선)/`_PATH`(폴백) 지원, `docker-compose.prod.yml` 가 `infra/.env` 로 자동 주입.
  남은 것: prod `.env`(`ONEBITE_ENV_B64`)에 service account base64 실값 주입(운영)
- [~] 위치 기반 트리거 알림 ("근처 N미터 내 새 반띵") — 코드 완성 (DeviceLocationQuery 전략 패턴)
- [x] **인앱 채팅** (2026-06-15) — 반띵 단위 그룹챗, WebSocket+STOMP + REST + FCM 푸시. (V8)
- [x] **거래 완료 인증** — split 라이프사이클(`/complete`·`/report-broken`·`/leave`) + 공개 신뢰 프로필(`GET /users/{id}/trust`) + 모바일 액션바/배지. (V5)
- [x] **신고/차단** — `report/` 도메인 + 모바일 `features/report`. (V6)
- [x] **상품 카테고리 & 검색** — Split category enum + `GET /splits?category=&q=` + 모바일 필터칩/검색. (V7)
- [ ] PG 에스크로 연동 (안전거래) — 전자금융거래법 검토 필요. 실 PG 계약 전까지 보류

---

## Phase 3 — 성장 (미착수)

- [ ] 백그라운드 위치 추적 (지나가다가 알림)
- [ ] 단골 매장 & 정기 반띵
- [ ] 커뮤니티
- [ ] 통계/분석
