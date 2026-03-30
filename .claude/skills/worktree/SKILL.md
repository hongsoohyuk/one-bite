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
