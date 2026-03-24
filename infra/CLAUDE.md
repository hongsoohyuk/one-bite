# Infra - Docker & 배포

## 기술 스택

| 항목 | 기술 | 비고 |
|------|------|------|
| 컨테이너 | Docker + Docker Compose | |
| DB | PostgreSQL 16 + PostGIS 3.4 | 위치 기반 쿼리 |
| 런타임 | Eclipse Temurin JDK/JRE 17 | 멀티스테이지 빌드 |

## 디렉토리 구조

```
프로젝트 루트/
├── docker-compose.yml          # 개발 환경
├── docker-compose.prod.yml     # 운영 환경
├── Makefile                    # 편의 명령어
├── infra/
│   └── .env.example            # 환경변수 템플릿
└── server/
    ├── Dockerfile              # 멀티스테이지 빌드
    └── .dockerignore
```

## 실행 명령어 (Makefile)

| 명령어 | 설명 |
|--------|------|
| `make dev` | 개발 환경 기동 (docker compose up --build -d) |
| `make dev-down` | 개발 환경 종료 |
| `make prod` | 운영 환경 기동 (docker-compose.prod.yml) |
| `make prod-down` | 운영 환경 종료 |
| `make build` | JAR 로컬 빌드 (gradlew bootJar) |
| `make logs` | 전체 로그 |
| `make logs-server` | 서버 로그만 |
| `make logs-db` | DB 로그만 |
| `make clean` | 전체 정리 (volumes + gradle clean) |

## 환경 구성

### 개발 (docker-compose.yml)
- **DB**: postgis/postgis:16-3.4, 포트 5432, 크레덴셜 하드코딩 (onebite/onebite-dev-pass)
- **서버**: Dockerfile 빌드, 포트 8080, Spring profile=prod
- **JWT**: 개발용 시크릿 하드코딩
- **헬스체크**: DB → pg_isready (5s 간격)

### 운영 (docker-compose.prod.yml)
- **DB**: 환경변수로 크레덴셜 주입, 리소스 제한 (512M, 0.5 CPU)
- **서버**: `infra/.env` 파일에서 설정 로드, 리소스 제한 (768M, 1.0 CPU)
- **헬스체크**: DB + 서버 (/actuator/health, 30s 간격, 40s 시작 유예)
- **재시작**: `restart: always`

### 환경변수 (infra/.env.example)
```
DB_NAME, DB_USERNAME, DB_PASSWORD
JWT_SECRET, JWT_EXPIRATION
KAKAO_CLIENT_ID, KAKAO_REDIRECT_URI
NAVER_CLIENT_ID, NAVER_CLIENT_SECRET
GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, GOOGLE_REDIRECT_URI
APPLE_CLIENT_ID
```

## Docker 빌드 (server/Dockerfile)

멀티스테이지 빌드:
1. **Builder** (temurin:17-jdk): Gradle 래퍼 → 의존성 다운로드 → bootJar
2. **Runtime** (temurin:17-jre): JAR 복사 → `java -jar app.jar`

## 운영 배포 절차

```bash
cp infra/.env.example infra/.env   # 환경변수 설정
# infra/.env 편집 (실제 크레덴셜 입력)
make prod                           # 운영 기동
make logs                           # 로그 모니터링
```

## 구현 상태

### 완료
- [x] Docker Compose 개발 환경 (PostgreSQL + PostGIS + Spring Boot)
- [x] Docker Compose 운영 환경 (리소스 제한, 헬스체크, 재시작 정책)
- [x] 멀티스테이지 Dockerfile (이미지 최적화)
- [x] Makefile 편의 명령어
- [x] 환경변수 템플릿 (.env.example)
- [x] Spring 프로필 분리 (default/prod)

### TODO
- [ ] **CI/CD 파이프라인** — GitHub Actions (빌드, 테스트, 이미지 푸시, 배포)
- [ ] **리버스 프록시** — Nginx 또는 Traefik (SSL 종단, 로드밸런싱)
- [ ] **SSL/TLS** — Let's Encrypt 인증서 자동 갱신
- [ ] **모니터링** — Prometheus + Grafana (메트릭 수집/대시보드)
- [ ] **로깅** — ELK 또는 Loki (중앙 로그 수집)
- [ ] **DB 마이그레이션** — Flyway 또는 Liquibase 스크립트
- [ ] **시크릿 관리** — AWS Secrets Manager / Vault (하드코딩 제거)
- [ ] **클라우드 배포** — AWS ECS/EKS 또는 GCP Cloud Run
- [ ] **백업** — PostgreSQL 자동 백업 + 복원 절차
