# Monorepo Management Design

## 배경

One Bite는 4개의 독립 프로젝트(server, mobile, web, infra)를 하나의 레포에서 관리하는 모노레포 구조다. 프로젝트 간 코드 공유는 없고, 빌드 시스템도 각각 다르다(Gradle, Gradle, npm, Terraform).

### 현재 문제점
1. **컨텍스트 혼선**: Claude Code에서 server와 mobile을 동시에 작업시키면 컨텍스트가 섞임. 루트에서 오케스트레이션하려 해도 결국 `server/`로 들어가서 작업하게 됨.
2. **CI 비효율**: server만 변경해도 전체 빌드가 돌아갈 수 있는 구조 (현재 CI 미구성 상태).
3. **미래 협업 대비**: 팀원이 추가될 경우 영역별 독립 작업이 가능해야 함.

### 결정: 모노레포 유지
- 프로젝트 간 코드 공유 없음 — 분리 실익 없음
- 35커밋 규모에서 레포 분리는 관리 포인트만 4배로 증가
- `docs/api-spec.md` 등 공유 리소스가 한 곳에 있는 게 유리
- 나중에 분리 필요 시 `git filter-repo`로 추출 가능

---

## 설계

### 1. Makefile 확장 — 영역별 태스크 러너

루트 Makefile에 영역별 빌드/테스트/린트 명령을 추가한다. 기존 docker 관련 명령은 유지.

```makefile
# === 영역별 빌드 ===
build-server:
	cd server && ./gradlew bootJar

build-mobile:
	cd mobile && ./gradlew build

build-web:
	cd web && npm run build

# === 영역별 테스트 ===
test-server:
	cd server && ./gradlew test

test-mobile:
	cd mobile && ./gradlew allTests

test-web:
	cd web && npm test

# === 영역별 lint ===
lint-server:
	cd server && ./gradlew ktlintCheck

lint-mobile:
	cd mobile && ./gradlew ktlintCheck

# === 전체 ===
build-all: build-server build-mobile build-web
test-all: test-server test-mobile test-web
```

**목적:**
- 루트에서 `make test-server`로 특정 영역만 실행
- CI에서 동일한 명령 재사용
- 에이전트가 루트에서 오케스트레이션할 때 진입점 역할

### 2. CI/CD — GitHub Actions path filter

변경된 파일 경로에 따라 해당 영역의 워크플로만 실행한다.

```yaml
# .github/workflows/server.yml
name: Server CI
on:
  push:
    paths: ['server/**', 'docs/api-spec.md']
  pull_request:
    paths: ['server/**', 'docs/api-spec.md']

# .github/workflows/mobile.yml
name: Mobile CI
on:
  push:
    paths: ['mobile/**', 'docs/api-spec.md']
  pull_request:
    paths: ['mobile/**', 'docs/api-spec.md']

# .github/workflows/web.yml
name: Web CI
on:
  push:
    paths: ['web/**']
  pull_request:
    paths: ['web/**']
```

**규칙:**
- `docs/api-spec.md` 변경 시 server + mobile 양쪽 CI 트리거
- web은 API spec과 무관하게 독립 실행
- infra는 별도 워크플로 필요 시 추후 추가

### 3. 병렬 작업 — worktree 관리 스킬

Claude Code 스킬(`/worktree`)을 만들어 영역별 독립 작업 공간을 자동 관리한다.

**사용법:**
```
/worktree server "Split API에 검색 필터 추가해줘"
/worktree mobile "검색 UI 만들어줘"
/worktree clean
```

**스킬 동작:**
1. `git worktree list`로 기존 worktree 확인
2. 없으면 `git worktree add ../one-bite-{영역}-wt main` 실행
3. 있으면 기존 worktree 재사용
4. 해당 worktree 경로에서 에이전트를 spawn하여 독립 작업
5. `/worktree clean`으로 완료된 worktree 정리

**결과 디렉토리 구조:**
```
~/dev/toy/
├── one-bite/              # 메인 레포 (루트 오케스트레이션)
├── one-bite-server-wt/    # server 전용 작업 공간
└── one-bite-mobile-wt/    # mobile 전용 작업 공간
```

**사용 시점:**
- 평소에는 메인 레포에서 `make test-server` 등으로 작업
- server + mobile을 동시에 병렬 작업해야 할 때만 worktree 사용

---

## 범위 밖 (의도적으로 제외)

- **모노레포 패키지 매니저 (Nx, Bazel 등)**: 프로젝트 간 의존성이 없어 불필요
- **레포 분리**: 현재 규모에서 실익 없음. 팀이 커지면 재검토
- **OpenAPI 코드 생성**: 별도 주제로 다룰 것
- **MSA 전환**: 현재 모놀리스로 충분. Phase 2 이후 필요 시 패키지 경계에서 분리
