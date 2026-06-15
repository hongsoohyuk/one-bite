import { describe, it, expect, vi, beforeEach } from 'vitest';
import { type ReactNode } from 'react';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

vi.mock('../../shared/api/nthingApi', () => ({
  nthingApi: {
    getChatMessages: vi.fn(),
    sendChatMessage: vi.fn(),
    markChatRead: vi.fn(),
    getChatUnread: vi.fn(),
  },
}));

import { nthingApi } from '../../shared/api/nthingApi';
import {
  chatKeys,
  useChatMessages,
  useSendChatMessage,
  useChatUnread,
  useMarkChatRead,
} from './queries';
import { type ChatMessage } from '../../shared/api/types';

const api = nthingApi as unknown as Record<string, ReturnType<typeof vi.fn>>;

function makeWrapper() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return {
    qc,
    wrapper: ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={qc}>{children}</QueryClientProvider>
    ),
  };
}

function msg(id: number, senderId = 9): ChatMessage {
  return {
    id,
    splitId: 1,
    senderId,
    senderNickname: '상대',
    senderProfileImageUrl: null,
    content: `m$id`,
    createdAt: '2026-06-15T00:00:00',
  };
}

describe('chat queries', () => {
  beforeEach(() => vi.clearAllMocks());

  it('useChatMessages 는 getChatMessages 결과 반환', async () => {
    api.getChatMessages.mockResolvedValue([msg(2), msg(1)]);
    const { wrapper } = makeWrapper();
    const { result } = renderHook(() => useChatMessages(1), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(api.getChatMessages).toHaveBeenCalledWith(1);
    expect(result.current.data?.map((m) => m.id)).toEqual([2, 1]);
  });

  it('useSendChatMessage 성공 시 새 메시지를 캐시 맨 앞에 추가', async () => {
    const sent = msg(3, 1);
    api.sendChatMessage.mockResolvedValue(sent);
    const { qc, wrapper } = makeWrapper();
    qc.setQueryData(chatKeys.messages(1), [msg(2), msg(1)]);
    const { result } = renderHook(() => useSendChatMessage(1), { wrapper });
    result.current.mutate('안녕');
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(api.sendChatMessage).toHaveBeenCalledWith(1, { content: '안녕' });
    expect(qc.getQueryData<ChatMessage[]>(chatKeys.messages(1))?.map((m) => m.id)).toEqual([
      3, 2, 1,
    ]);
  });

  it('같은 id 는 중복 추가하지 않음(STOMP echo 대비)', async () => {
    const dup = msg(2);
    api.sendChatMessage.mockResolvedValue(dup);
    const { qc, wrapper } = makeWrapper();
    qc.setQueryData(chatKeys.messages(1), [msg(2), msg(1)]);
    const { result } = renderHook(() => useSendChatMessage(1), { wrapper });
    result.current.mutate('again');
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(qc.getQueryData<ChatMessage[]>(chatKeys.messages(1))?.map((m) => m.id)).toEqual([2, 1]);
  });

  it('useChatUnread 는 enabled=false 면 호출 안 함', () => {
    const { wrapper } = makeWrapper();
    renderHook(() => useChatUnread(1, false), { wrapper });
    expect(api.getChatUnread).not.toHaveBeenCalled();
  });

  it('useMarkChatRead 성공 시 unread 캐시를 0 으로', async () => {
    api.markChatRead.mockResolvedValue(undefined);
    const { qc, wrapper } = makeWrapper();
    qc.setQueryData(chatKeys.unread(1), { count: 5 });
    const { result } = renderHook(() => useMarkChatRead(1), { wrapper });
    result.current.mutate();
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(qc.getQueryData(chatKeys.unread(1))).toEqual({ count: 0 });
  });
});
