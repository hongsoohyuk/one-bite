import { Client } from '@stomp/stompjs';
import { env } from '../../shared/lib/env';
import { type ChatMessage } from '../../shared/api/types';

export function chatTopic(splitId: number): string {
  return `/topic/chats/${splitId}`;
}

type ChatClientOptions = {
  onMessage: (m: ChatMessage) => void;
  onStatus?: (connected: boolean) => void;
};

/**
 * split 채팅방용 STOMP 클라이언트. CONNECT 헤더로 JWT 를 실어 인증하고
 * 연결되면 `/topic/chats/{splitId}` 를 구독한다. (전송은 REST, 수신만 STOMP)
 */
export function createChatClient(
  token: string,
  splitId: number,
  { onMessage, onStatus }: ChatClientOptions,
): Client {
  const client = new Client({
    brokerURL: env.wsUrl,
    connectHeaders: { Authorization: `Bearer ${token}` },
    reconnectDelay: 3000,
  });

  client.onConnect = () => {
    onStatus?.(true);
    client.subscribe(chatTopic(splitId), (frame) => {
      try {
        onMessage(JSON.parse(frame.body) as ChatMessage);
      } catch {
        // 깨진 프레임 무시
      }
    });
  };
  client.onWebSocketClose = () => onStatus?.(false);
  client.onStompError = () => onStatus?.(false);

  return client;
}
