# 반띵 채팅방 설계 (2026-06-15)

참여가 일어난 반띵(split) 단위로 그룹 채팅방을 제공한다. Phase 2 "인앱 채팅".

## 배경 / 시나리오
- 유저가 반띵에 참여(join)하면 그 반띵의 당사자(주최자 + 참여자)끼리 만날 장소·시간을
  조율할 채팅이 필요하다.
- 채팅방은 **반띵 1개 = 방 1개** (1:1). 별도 방 개설 액션 없이, 멤버면 바로 입장.

## 핵심 결정

| 결정 | 내용 | 이유 |
|------|------|------|
| 방 식별 | 별도 `chat_room` 테이블 없이 **splitId 를 방 키로** 사용 | 방은 split 과 1:1 → 테이블/지연생성 로직 불필요(서버 단순화 기조) |
| 멤버십 | 주최자 + 활성 참여자(JOINED/COMPLETED). LATE_CANCELLED 는 비멤버 | split 도메인 재사용, 별도 멤버 테이블 불필요 |
| 실시간 전송 | **WebSocket + STOMP** (`/ws`, simple broker `/topic`) | 사용자 결정. 진짜 실시간 UX |
| 전송 경로 | **보내기는 REST(POST)**, **받기는 STOMP 구독** | 전송은 기존 JWT/REST 로 신뢰성·영속성 보장. 구독은 실시간 수신 전용 |
| 브로드캐스트/푸시 | `ChatMessageCreatedEvent` → AFTER_COMMIT + @Async 리스너에서 `/topic` 브로드캐스트 + FCM | 기존 알림 디커플링 패턴 그대로. 롤백 시 유령 메시지 방지 |
| STOMP 인증 | CONNECT 프레임의 `Authorization: Bearer` → JwtProvider 검증 → Principal. SUBSCRIBE 시 토픽 splitId 멤버십 검사 | 비멤버가 남의 방 토픽 구독 차단 |
| 새 메시지 푸시 | 기존 FCM 단일 채널 재사용, 발신자 제외 멤버에게 `CHAT_MESSAGE` | 앱 미접속 시에도 알림 |
| 읽음 처리 | `chat_read_states(split_id, user_id, last_read_at)` upsert + unread count | 안읽음 배지(SplitDetail 진입점) |
| 마이그레이션 | **V8** = chat_messages, chat_read_states | Flyway 버전 충돌 차단 |

## 서버 API (`/api/splits/{splitId}/chat`, 모두 멤버만)

| Method | Path | 설명 |
|--------|------|------|
| GET | `/messages?before={id}&size=30` | 메시지 히스토리(최신순, 커서=id) |
| POST | `/messages` `{content}` | 전송 → 영속 + 이벤트(브로드캐스트/푸시). 201 |
| POST | `/read` | 내 읽음 위치를 now 로 갱신(upsert) |
| GET | `/unread` | 내 안읽음 수 `{count}` (내 메시지 제외) |

STOMP: 클라이언트 `/ws` 연결(CONNECT 헤더 Authorization) → `/topic/chats/{splitId}` 구독 → 새 메시지 수신.

### 데이터
```
chat_messages(id, split_id FK, sender_id FK, content VARCHAR(1000), created_at)
chat_read_states(id, split_id, user_id FK, last_read_at, UNIQUE(split_id,user_id))
```

## 모바일

- 의존성 `@stomp/stompjs` (raw WebSocket, SockJS 불필요 — Capacitor 웹뷰 네이티브 WS 지원).
- `features/chat/`: `chatSocket`(STOMP 연결/구독), `queries`(useChatMessages/useSendMessage/useChatUnread/useMarkRead), `ChatRoom` 화면, 메시지 버블/컴포저.
- 라우트 `/splits/:id/chat` (셸 없는 풀스크린).
- 진입점: `SplitDetail` 의 멤버에게 "채팅" 버튼 + 안읽음 배지.
- WS BASE: dev=Vite proxy(`/ws`), prod=`wss://api.nthing.app/ws`.
- i18n: `chat.*` 4개 로케일.

## 범위(MVP) / 후속
- MVP: 텍스트 메시지, 실시간 수신, 히스토리, 안읽음, 새 메시지 푸시.
- 후속: 이미지/위치 공유, 타이핑 표시, 읽음(상대가 읽음) 표시, 방 목록 탭.
