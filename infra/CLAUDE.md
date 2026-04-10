# Infra - Docker & 배포

## 기술 스택

| 항목 | 기술 | 비고 |
|------|------|------|
| 클라우드 | AWS EC2 (Seoul) | ap-northeast-2 |
| IaC | Terraform (AWS Provider) | Default VPC 사용 |
| 컨테이너 | Docker + Docker Compose | |
| 리버스 프록시 | Nginx + Certbot (Let's Encrypt) | SSL 종단 |
| CI/CD | GitHub Actions + GHCR | 이미지 빌드/푸시/SSH 배포 |
| DB | PostgreSQL 16 + PostGIS 3.4 | 위치 기반 쿼리 |
| 런타임 | Eclipse Temurin JRE 17 (jammy) | 멀티스테이지 빌드 |
| OS | Amazon Linux 2023 (arm64) | Graviton2 호환 |

## 디렉토리 구조

```
프로젝트 루트/
├── docker-compose.yml          # 개발 환경
├── docker-compose.prod.yml     # 운영 환경 (nginx, certbot, server, db)
├── Makefile                    # 편의 명령어
├── .github/workflows/
│   └── deploy.yml              # GHCR 푸시 + SSH 배포 + 헬스체크
├── infra/
│   ├── CLAUDE.md               # 이 문서
│   ├── .env.example            # 환경변수 템플릿
│   ├── nginx/
│   │   └── nginx.conf          # 리버스 프록시 설정
│   ├── scripts/
│   │   └── init-ssl.sh         # 최초 Let's Encrypt 발급 스크립트
│   ├── certbot/                # 인증서 저장 디렉토리
│   └── terraform/
│       ├── main.tf             # AWS 리소스 (SG, Key Pair, EC2, EIP)
│       ├── variables.tf        # 변수 정의
│       ├── outputs.tf          # 출력값
│       ├── user-data.sh        # EC2 초기화 (Docker 설치)
│       └── .gitignore
└── server/
    ├── Dockerfile              # 멀티스테이지 빌드 + HEALTHCHECK
    └── .dockerignore
```

## AWS 인프라 스펙

| 리소스 | 사양 | 월 예상 비용 |
|--------|------|--------------|
| EC2 t4g.small | 2 vCPU ARM Graviton2 / 2GB RAM | ~$15 |
| EBS gp3 | 30GB | ~$2.4 |
| Elastic IP | 인스턴스 연결 중 무료, 분리 시 시간당 $0.005 | - |
| Egress | 100GB/월 무료 이후 $0.126/GB | - |
| **합계** | | **~$17-20/월** |

Default VPC/Subnet을 사용해 네트워크 리소스는 직접 생성하지 않음.

## Terraform 사용법

### 사전 준비

1. AWS CLI 설치 + profile 설정: `aws configure --profile personal`
2. SSH 접근 허용할 내 IP 확보 (CIDR `1.2.3.4/32` 형식)
3. `terraform.tfvars` 작성 (아래 참고)

### 실행

```bash
cd infra/terraform
terraform init          # AWS provider 다운로드
terraform plan          # 변경사항 확인
terraform apply         # 리소스 생성
terraform destroy       # 리소스 삭제
```

### 주요 변수 (terraform.tfvars)

```hcl
my_ip         = "1.2.3.4/32"        # SSH 접근 허용할 내 IP (필수)
aws_profile   = "personal"          # 기본값
aws_region    = "ap-northeast-2"    # 기본값
instance_type = "t4g.small"         # 기본값
project_name  = "onebite"           # 기본값
```

### SSH 키 추출

`tls_private_key`로 자동 생성된 키를 로컬 파일로 저장:

```bash
terraform output -raw private_key > onebite-key.pem
chmod 600 onebite-key.pem
ssh -i onebite-key.pem ec2-user@$(terraform output -raw public_ip)
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
- **nginx**: 80/443 포트 노출, 리버스 프록시 → `server:8080`
- **certbot**: Let's Encrypt 인증서 발급/갱신 (볼륨 공유)
- **DB**: 환경변수로 크레덴셜 주입, 리소스 제한 (512M, 0.5 CPU)
- **서버**: `infra/.env` 로드, 리소스 제한 (768M, 1.0 CPU), 내부 네트워크만 노출
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
1. **Builder** (temurin:17-jdk-jammy): Gradle 래퍼 → 의존성 다운로드 → bootJar
2. **Runtime** (temurin:17-jre-jammy): JAR 복사 → `java -jar app.jar`
3. **HEALTHCHECK**: `/actuator/health` 주기적 호출

## CI/CD 파이프라인 (.github/workflows/deploy.yml)

1. main 브랜치 푸시 (paths: `server/**`, `docker-compose.prod.yml`, `infra/nginx/**`, `.github/workflows/deploy.yml`) 또는 수동 트리거
2. Docker 이미지 **linux/arm64**로 빌드 → GHCR(`ghcr.io/<owner>/onebite/server:latest`) 푸시
3. **GitHub OIDC**로 AWS IAM role 가정 (`onebite-github-actions-role`)
4. **AWS SSM Run Command**로 EC2에 배포 스크립트 실행
   - `git pull` → `docker login ghcr.io` → `docker compose --env-file ./infra/.env pull/up -d` → `docker image prune`
5. `/actuator/health` 재시도 헬스체크 (HTTP 80, nginx 경유)

### GitHub Secrets
- `AWS_ROLE_TO_ASSUME` — `arn:aws:iam::<account>:role/onebite-github-actions-role`
- `EC2_HOST` — Elastic IP (health check 및 로그 보고용)
- (GHCR 인증은 GitHub 기본 토큰 사용)

### GitHub Actions → AWS 인증 흐름
- Terraform이 `aws_iam_openid_connect_provider.github`와 GitHub Actions용 role(`onebite-github-actions-role`)을 생성
- Role의 trust policy가 `repo:hongsoohyuk/one-bite:*` sub claim을 허용
- 부여된 권한: `ssm:SendCommand`, `ssm:GetCommandInvocation`, `ssm:ListCommandInvocations`
- EC2에는 `onebite-ec2-ssm-profile` instance profile이 붙어 `AmazonSSMManagedInstanceCore`로 SSM과 통신

### 인스턴스 ID 하드코딩
`deploy.yml`의 `env.EC2_INSTANCE_ID`는 현재 인스턴스 ID로 하드코딩되어 있습니다. 인스턴스를 재생성하면 이 값도 같이 업데이트해야 합니다.

## 운영 배포 절차

### 최초 1회 (인프라 프로비저닝)
```bash
cd infra/terraform
terraform init && terraform apply

terraform output -raw private_key > onebite-key.pem
chmod 600 onebite-key.pem

# 로컬 SSH 접속 (my_ip 화이트리스트 유지)
ssh -i onebite-key.pem ec2-user@$(terraform output -raw public_ip)

# 또는 AWS SSM Session Manager로 접속 (권장)
aws ssm start-session --target $(terraform output -raw instance_id) --profile personal --region ap-northeast-2
```

### 최초 1회 (저장소 bootstrap)
```bash
# 로컬에서
scp -i onebite-key.pem infra/.env ec2-user@<eip>:/tmp/onebite.env

# 서버 안에서
sudo git clone https://github.com/hongsoohyuk/one-bite.git /opt/onebite
sudo chown -R ec2-user:ec2-user /opt/onebite
sudo mv /tmp/onebite.env /opt/onebite/infra/.env
sudo chown ec2-user:ec2-user /opt/onebite/infra/.env
sudo chmod 600 /opt/onebite/infra/.env
```

### 최초 1회 (SSL 발급, 도메인 준비 후)
현재는 HTTP-only nginx 구성. 도메인이 준비되면:
1. `infra/nginx/nginx.conf`에 443 server 블록과 80→443 redirect 복원
2. 서버에서 `./infra/scripts/init-ssl.sh <도메인>` 실행
3. OAuth provider 콘솔에서 redirect URI를 HTTPS/도메인 기반으로 업데이트
4. `infra/.env`의 `*_REDIRECT_URI`도 같이 업데이트 후 재배포

### 일상 배포
GitHub Actions가 main 브랜치 푸시 시 자동으로 배포합니다. 수동 롤아웃은 Actions 탭에서 `Deploy to AWS` 워크플로우를 재실행.

## 구현 상태

### 완료
- [x] Docker Compose 개발 환경 (PostgreSQL + PostGIS + Spring Boot)
- [x] Docker Compose 운영 환경 (nginx + certbot + server + db)
- [x] 멀티스테이지 Dockerfile + HEALTHCHECK
- [x] Makefile 편의 명령어
- [x] 환경변수 템플릿 (.env.example)
- [x] Spring 프로필 분리 (default/prod)
- [x] AWS Terraform (Default VPC + SG + Key Pair + EC2 t4g.small + EIP)
- [x] GitHub Actions 배포 파이프라인 (GHCR + SSH)
- [x] Nginx 리버스 프록시 설정
- [x] Let's Encrypt 발급 스크립트 (init-ssl.sh)

### TODO
- [ ] **도메인 연결** — Route53 또는 외부 DNS에서 A 레코드를 EIP로 지정
- [ ] **HTTPS 활성화** — 도메인 확보 후 `init-ssl.sh` 실행
- [ ] **모바일 API base URL을 도메인 기반으로 전환** — 현재 EIP 하드코딩
- [ ] **모니터링** — Prometheus + Grafana
- [ ] **로깅** — ELK 또는 Loki
- [ ] **백업** — PostgreSQL 자동 백업 + 복원 절차
