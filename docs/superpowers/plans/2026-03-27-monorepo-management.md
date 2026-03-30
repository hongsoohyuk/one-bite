# Monorepo Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 모노레포를 유지하면서 영역별 독립 빌드/CI/작업 환경을 구축한다.

**Architecture:** 루트 Makefile을 영역별 태스크 러너로 확장하고, GitHub Actions를 path filter 기반으로 영역별 분리하며, Claude Code 스킬(`/worktree`)로 worktree 기반 병렬 작업을 자동화한다.

**Tech Stack:** Make, GitHub Actions, git worktree, Claude Code Skills

---

## File Structure

| Action | Path | Responsibility |
|--------|------|----------------|
| Modify | `Makefile` | 영역별 build/test/lint 명령 추가 |
| Rename | `.github/workflows/ci.yml` → `.github/workflows/server-ci.yml` | server 전용 CI (path filter 확장) |
| Create | `.github/workflows/mobile-ci.yml` | mobile 전용 CI |
| Create | `.github/workflows/web-ci.yml` | web 전용 CI |
| Create | `.claude/skills/worktree/SKILL.md` | worktree 관리 스킬 |

기존 `docker-publish.yml`과 `deploy.yml`은 server 전용이므로 변경 없음.

---

### Task 1: Makefile 확장

**Files:**
- Modify: `Makefile`

- [ ] **Step 1: 현재 Makefile 확인 및 .PHONY 확장**

`.PHONY` 선언에 새 타겟들을 추가하고, 기존 `build` 타겟을 `build-server`로 대체한다. 기존 docker/logs/clean 명령은 그대로 유지.

```makefile
.PHONY: dev dev-down prod prod-down build-server build-mobile build-web build-all test-server test-mobile test-web test-all lint-server lint-mobile logs logs-server logs-db clean
```

- [ ] **Step 2: 영역별 빌드 타겟 추가**

기존 `build:` 타겟을 제거하고 아래로 교체한다. Makefile 하단(clean 아래)에 추가:

```makefile
# === 영역별 빌드 ===
build-server:
	cd server && ./gradlew bootJar

build-mobile:
	cd mobile && ./gradlew build

build-web:
	cd web && npm run build

build-all: build-server build-mobile build-web
```

- [ ] **Step 3: 영역별 테스트 타겟 추가**

```makefile
# === 영역별 테스트 ===
test-server:
	cd server && ./gradlew test

test-mobile:
	cd mobile && ./gradlew allTests

test-web:
	cd web && npm test

test-all: test-server test-mobile test-web
```

- [ ] **Step 4: 영역별 lint 타겟 추가**

```makefile
# === 영역별 lint ===
lint-server:
	cd server && ./gradlew ktlintCheck

lint-mobile:
	cd mobile && ./gradlew ktlintCheck
```

- [ ] **Step 5: make 타겟 동작 확인**

```bash
make -n build-server
make -n test-server
make -n build-all
```

Expected: 각각 `cd server && ./gradlew bootJar`, `cd server && ./gradlew test`, 3개 빌드 명령이 dry-run으로 출력됨.

- [ ] **Step 6: Commit**

```bash
git add Makefile
git commit -m "chore: extend Makefile with per-area build/test/lint targets"
```

---

### Task 2: Server CI 워크플로 리네임 + path filter 확장

**Files:**
- Rename: `.github/workflows/ci.yml` → `.github/workflows/server-ci.yml`

기존 `ci.yml`은 이미 `server/**` path filter가 있다. 이름을 `server-ci.yml`로 바꾸고 `docs/api-spec.md` path를 추가한다.

- [ ] **Step 1: ci.yml을 server-ci.yml로 이동**

```bash
git mv .github/workflows/ci.yml .github/workflows/server-ci.yml
```

- [ ] **Step 2: workflow name과 path filter 수정**

`server-ci.yml`을 편집:

```yaml
name: Server CI

on:
  push:
    branches: [main, develop]
    paths:
      - 'server/**'
      - 'docs/api-spec.md'
      - '.github/workflows/server-ci.yml'
  pull_request:
    branches: [main, develop]
    paths:
      - 'server/**'
      - 'docs/api-spec.md'
      - '.github/workflows/server-ci.yml'

permissions:
  contents: read

jobs:
  build:
    name: Build & Test
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: server

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Build
        run: ./gradlew build --no-daemon

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: server/build/reports/tests/
          retention-days: 7
```

- [ ] **Step 3: Commit**

```bash
git add .github/workflows/
git commit -m "ci: rename ci.yml to server-ci.yml, add api-spec.md path trigger"
```

---

### Task 3: Mobile CI 워크플로 생성

**Files:**
- Create: `.github/workflows/mobile-ci.yml`

- [ ] **Step 1: mobile-ci.yml 작성**

```yaml
name: Mobile CI

on:
  push:
    branches: [main, develop]
    paths:
      - 'mobile/**'
      - 'docs/api-spec.md'
      - '.github/workflows/mobile-ci.yml'
  pull_request:
    branches: [main, develop]
    paths:
      - 'mobile/**'
      - 'docs/api-spec.md'
      - '.github/workflows/mobile-ci.yml'

permissions:
  contents: read

jobs:
  build:
    name: Build & Test
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: mobile

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Build
        run: ./gradlew build --no-daemon

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: mobile-test-results
          path: mobile/build/reports/tests/
          retention-days: 7
```

- [ ] **Step 2: Commit**

```bash
git add .github/workflows/mobile-ci.yml
git commit -m "ci: add mobile-ci.yml with path filter"
```

---

### Task 4: Web CI 워크플로 생성

**Files:**
- Create: `.github/workflows/web-ci.yml`

- [ ] **Step 1: web-ci.yml 작성**

```yaml
name: Web CI

on:
  push:
    branches: [main, develop]
    paths:
      - 'web/**'
      - '.github/workflows/web-ci.yml'
  pull_request:
    branches: [main, develop]
    paths:
      - 'web/**'
      - '.github/workflows/web-ci.yml'

permissions:
  contents: read

jobs:
  build:
    name: Build
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: web

    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: web/package-lock.json

      - name: Install dependencies
        run: npm ci

      - name: Build
        run: npm run build
```

- [ ] **Step 2: Commit**

```bash
git add .github/workflows/web-ci.yml
git commit -m "ci: add web-ci.yml with path filter"
```

---

### Task 5: Worktree 관리 스킬 생성

**Files:**
- Create: `.claude/skills/worktree/SKILL.md`

- [ ] **Step 1: 스킬 파일 작성**

```markdown
---
name: worktree
description: Git worktree를 사용해 영역별 독립 작업 공간을 자동 생성/관리한다
invocation: worktree
---

# Worktree Management Skill

모노레포에서 영역(server, mobile, web, infra)별로 독립 git worktree를 생성하고, 해당 worktree에서 에이전트를 spawn하여 병렬 작업을 수행한다.

## 사용법

- `/worktree server "작업 설명"` — server 영역 worktree에서 에이전트 실행
- `/worktree mobile "작업 설명"` — mobile 영역 worktree에서 에이전트 실행
- `/worktree web "작업 설명"` — web 영역 worktree에서 에이전트 실행
- `/worktree clean` — 완료된 worktree 정리
- `/worktree list` — 현재 worktree 목록 확인

## 동작 절차

### 영역 worktree 생성 및 에이전트 실행

1. 인자를 파싱한다: `{area}` (server|mobile|web|infra)와 `{task_description}` (따옴표 안 문자열)
2. `git worktree list`로 기존 worktree 확인
3. `../one-bite-{area}-wt` 경로에 worktree가 없으면 생성:
   ```bash
   git worktree add ../one-bite-{area}-wt main
   ```
4. 이미 있으면 기존 worktree를 재사용한다
5. Agent 도구를 사용해 해당 worktree의 영역 디렉토리에서 에이전트를 spawn:
   - prompt: task_description
   - 작업 디렉토리: `../one-bite-{area}-wt/{area}/`

### clean

1. `git worktree list`로 모든 worktree 확인
2. `one-bite-*-wt` 패턴의 worktree를 찾음
3. 각 worktree에서 커밋되지 않은 변경이 없는지 확인 (`git -C {path} status --porcelain`)
4. 깨끗한 worktree만 제거:
   ```bash
   git worktree remove ../one-bite-{area}-wt
   ```
5. 변경이 있는 worktree는 경고 메시지를 출력하고 남겨둔다

### list

1. `git worktree list` 실행
2. 결과를 사용자에게 보여준다

## 유효한 영역

- `server` — Spring Boot 백엔드
- `mobile` — KMP + Compose Multiplatform
- `web` — Next.js 랜딩 페이지
- `infra` — Terraform 인프라

이 외의 영역이 입력되면 에러 메시지를 출력한다.

## 주의사항

- worktree는 병렬 작업이 필요할 때만 사용한다. 단일 영역 작업은 메인 레포에서 직접.
- 각 worktree는 독립 브랜치에서 작업하지 않음 — main을 체크아웃한 상태에서 시작. 에이전트가 필요시 브랜치를 생성.
- worktree 안에서의 커밋은 메인 레포에서 `git log --all`로 볼 수 있다.
```

- [ ] **Step 2: 스킬 동작 확인**

스킬이 잘 인식되는지 확인:

```bash
cat .claude/skills/worktree/SKILL.md | head -5
```

Expected:
```
---
name: worktree
description: Git worktree를 사용해 영역별 독립 작업 공간을 자동 생성/관리한다
invocation: worktree
---
```

- [ ] **Step 3: Commit**

```bash
git add .claude/skills/worktree/SKILL.md
git commit -m "feat: add /worktree skill for parallel area-based development"
```
