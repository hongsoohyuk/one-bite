import { useEffect, useState } from 'react';
import { useMutation, useQuery, useQueryClient, type QueryClient } from '@tanstack/react-query';
import { nthingApi } from '../../shared/api/nthingApi';
import { type ChatMessage, type ChatUnreadResponse } from '../../shared/api/types';
import { useAuthStore } from '../../shared/stores/authStore';
import { createChatClient } from './chatSocket';

export const chatKeys = {
  messages: (splitId: number) => ['chat', splitId, 'messages'] as const,
  unread: (splitId: number) => ['chat', splitId, 'unread'] as const,
};

// 서버는 최신순(newest-first) 으로 내려줌. 캐시도 그 순서를 유지하고 id 로 중복 제거.
function appendMessage(qc: QueryClient, splitId: number, msg: ChatMessage) {
  qc.setQueryData<ChatMessage[]>(chatKeys.messages(splitId), (old = []) =>
    old.some((m) => m.id === msg.id) ? old : [msg, ...old],
  );
}

export function useChatMessages(splitId: number) {
  return useQuery<ChatMessage[]>({
    queryKey: chatKeys.messages(splitId),
    queryFn: () => nthingApi.getChatMessages(splitId),
    enabled: Number.isFinite(splitId),
  });
}

export function useSendChatMessage(splitId: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (content: string) => nthingApi.sendChatMessage(splitId, { content }),
    // 서버가 /topic 으로 본인에게도 echo 하지만, 미연결 폴백 대비 낙관적 반영(중복은 id 로 제거)
    onSuccess: (msg) => appendMessage(qc, splitId, msg),
  });
}

export function useChatUnread(splitId: number, enabled = true) {
  return useQuery<ChatUnreadResponse>({
    queryKey: chatKeys.unread(splitId),
    queryFn: () => nthingApi.getChatUnread(splitId),
    enabled: enabled && Number.isFinite(splitId),
  });
}

export function useMarkChatRead(splitId: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => nthingApi.markChatRead(splitId),
    onSuccess: () => {
      qc.setQueryData<ChatUnreadResponse>(chatKeys.unread(splitId), { count: 0 });
    },
  });
}

/**
 * 채팅방 화면에서 STOMP 실시간 수신을 켠다. 새 메시지는 캐시에 prepend.
 * @returns 연결 상태(connected)
 */
export function useChatSubscription(splitId: number): boolean {
  const qc = useQueryClient(); // provider 당 안정적 참조 → effect 의존성으로 안전
  const token = useAuthStore((s) => s.token);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    if (!token || !Number.isFinite(splitId)) return;
    const client = createChatClient(token, splitId, {
      onMessage: (m) => appendMessage(qc, splitId, m),
      onStatus: setConnected,
    });
    client.activate();
    return () => {
      setConnected(false);
      void client.deactivate();
    };
  }, [token, splitId, qc]);

  return connected;
}
