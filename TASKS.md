# Task: 나눠사기 참여(Join) API

## 브랜치: `feature/server-join`

## 목표
다른 유저가 등록된 Split에 참여(join)할 수 있는 API를 구현한다.

## 작업 범위: `server/` 디렉토리만 수정

## 상세 작업

### 1. SplitParticipant 엔티티 생성
- `server/src/main/kotlin/com/onebite/server/split/SplitParticipant.kt`
- 필드: `id`, `splitRequest: SplitRequest`, `user: User`, `joinedAt: LocalDateTime`
- `@ManyToOne`으로 SplitRequest, User 참조
- `(split_request_id, user_id)` 유니크 제약

### 2. SplitParticipantRepository 생성
- `server/src/main/kotlin/com/onebite/server/split/SplitParticipantRepository.kt`
- `findBySplitRequestId(splitRequestId: Long): List<SplitParticipant>`
- `existsBySplitRequestIdAndUserId(splitRequestId: Long, userId: Long): Boolean`

### 3. Join API 구현
- `POST /api/splits/{id}/join`
- 비즈니스 로직 (SplitService):
  - Split이 WAITING 상태인지 확인
  - 작성자 본인은 참여 불가
  - 이미 참여했으면 중복 참여 불가
  - 참여자 수가 `splitCount - 1`에 도달하면 상태를 `MATCHED`로 변경
- 응답: 참여 성공 시 200 + SplitResponse (업데이트된 상태 포함)

### 4. Cancel API 보완
- `PATCH /api/splits/{id}/cancel`
- 작성자만 취소 가능 (인증 정보 활용)
- WAITING 상태일 때만 취소 가능
- 취소 시 기존 참여자들에게 알림 (Phase 2)

### 5. SplitResponse에 참여자 정보 추가
- `participants: List<ParticipantDto>` 필드 추가
- `currentParticipants: Int` (현재 참여 인원)
- `ParticipantDto(userId, nickname, profileImageUrl, joinedAt)`

## 참고
- API 명세: `docs/api-spec.md`의 `POST /splits/{id}/join` 참조
- splitCount는 총 나누는 인원수 (작성자 포함)
- 예: splitCount=2이면 작성자 + 참여자 1명 → 참여자 1명이 join하면 MATCHED

## 검증
- `./gradlew build` 성공
- Join → 상태 MATCHED 전환 확인
- 중복 참여 시 400 에러
- 본인 Split에 참여 시 400 에러
- MATCHED 상태에서 추가 참여 시 400 에러
