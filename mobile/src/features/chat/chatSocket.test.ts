import { describe, it, expect, vi, beforeEach } from 'vitest';

vi.mock('@stomp/stompjs', () => {
  class Client {
    config: unknown;
    subscribe = vi.fn();
    activate = vi.fn();
    deactivate = vi.fn();
    onConnect?: () => void;
    onWebSocketClose?: () => void;
    onStompError?: () => void;
    constructor(config: unknown) {
      this.config = config;
    }
  }
  return { Client };
});

import { chatTopic, createChatClient } from './chatSocket';
import { type ChatMessage } from '../../shared/api/types';

type FakeClient = {
  config: { brokerURL: string; connectHeaders: Record<string, string> };
  subscribe: ReturnType<typeof vi.fn>;
  onConnect?: () => void;
  onWebSocketClose?: () => void;
};

describe('chatSocket', () => {
  beforeEach(() => vi.clearAllMocks());

  it('chatTopic 은 /topic/chats/{splitId}', () => {
    expect(chatTopic(7)).toBe('/topic/chats/7');
  });

  it('createChatClient 는 JWT 를 CONNECT 헤더로 싣는다', () => {
    const client = createChatClient('jwt-abc', 7, { onMessage: vi.fn() }) as unknown as FakeClient;
    expect(client.config.connectHeaders).toEqual({ Authorization: 'Bearer jwt-abc' });
    expect(client.config.brokerURL).toContain('/ws');
  });

  it('연결되면 토픽을 구독하고 메시지를 파싱해 콜백', () => {
    const onMessage = vi.fn();
    const onStatus = vi.fn();
    const client = createChatClient('jwt', 7, { onMessage, onStatus }) as unknown as FakeClient;

    client.onConnect?.();
    expect(onStatus).toHaveBeenCalledWith(true);
    expect(client.subscribe).toHaveBeenCalledWith('/topic/chats/7', expect.any(Function));

    const handler = client.subscribe.mock.calls[0][1] as (f: { body: string }) => void;
    const message: ChatMessage = {
      id: 1,
      splitId: 7,
      senderId: 9,
      senderNickname: '상대',
      senderProfileImageUrl: null,
      content: '안녕',
      createdAt: '2026-06-15T00:00:00',
    };
    handler({ body: JSON.stringify(message) });
    expect(onMessage).toHaveBeenCalledWith(message);
  });

  it('깨진 프레임은 무시(throw 안 함)', () => {
    const onMessage = vi.fn();
    const client = createChatClient('jwt', 7, { onMessage }) as unknown as FakeClient;
    client.onConnect?.();
    const handler = client.subscribe.mock.calls[0][1] as (f: { body: string }) => void;
    expect(() => handler({ body: 'not-json{' })).not.toThrow();
    expect(onMessage).not.toHaveBeenCalled();
  });
});
