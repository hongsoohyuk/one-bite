import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Routes, Route } from 'react-router-dom';

const send = vi.fn();
const markRead = vi.fn();
vi.mock('../features/chat/queries', () => ({
  useChatMessages: vi.fn(),
  useSendChatMessage: vi.fn(),
  useMarkChatRead: vi.fn(),
  useChatUnread: vi.fn(),
  useChatSubscription: vi.fn(),
}));
vi.mock('../features/splits/queries', () => ({ useSplit: vi.fn() }));

import {
  useChatMessages,
  useSendChatMessage,
  useMarkChatRead,
  useChatSubscription,
} from '../features/chat/queries';
import { useSplit } from '../features/splits/queries';
import { useAuthStore } from '../shared/stores/authStore';
import i18n from '../shared/i18n';
import { ChatRoom } from './ChatRoom';

const useMessagesMock = useChatMessages as unknown as ReturnType<typeof vi.fn>;
const useSendMock = useSendChatMessage as unknown as ReturnType<typeof vi.fn>;
const useMarkReadMock = useMarkChatRead as unknown as ReturnType<typeof vi.fn>;
const useSubMock = useChatSubscription as unknown as ReturnType<typeof vi.fn>;
const useSplitMock = useSplit as unknown as ReturnType<typeof vi.fn>;

function message(id: number, senderId: number, content: string) {
  return {
    id,
    splitId: 1,
    senderId,
    senderNickname: senderId === 1 ? '나' : '상대',
    senderProfileImageUrl: null,
    content,
    createdAt: '2026-06-15T00:00:00',
  };
}

function renderChat() {
  return render(
    <MemoryRouter initialEntries={['/splits/1/chat']}>
      <Routes>
        <Route path="/splits/:id/chat" element={<ChatRoom />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('ChatRoom', () => {
  beforeEach(async () => {
    await i18n.changeLanguage('ko');
    send.mockReset();
    markRead.mockReset();
    useSendMock.mockReturnValue({ mutate: send, isPending: false });
    useMarkReadMock.mockReturnValue({ mutate: markRead, isPending: false });
    useSubMock.mockReturnValue(true);
    useSplitMock.mockReturnValue({ data: { productName: '두쫀쿠' } });
    useAuthStore.setState({ token: 'jwt', user: { id: 1, nickname: '나' }, isHydrated: true });
  });

  it('메시지를 렌더하고 상대 닉네임을 표시', () => {
    useMessagesMock.mockReturnValue({
      isPending: false,
      isError: false,
      data: [message(2, 9, '몇 시에 만날까요?'), message(1, 1, '안녕하세요')],
    });
    renderChat();
    expect(screen.getByText('몇 시에 만날까요?')).toBeInTheDocument();
    expect(screen.getByText('안녕하세요')).toBeInTheDocument();
    expect(screen.getByText('상대')).toBeInTheDocument();
  });

  it('빈 상태 문구 표시 + 진입 시 읽음 처리는 호출 안 함(0건)', () => {
    useMessagesMock.mockReturnValue({ isPending: false, isError: false, data: [] });
    renderChat();
    expect(screen.getByText('아직 대화가 없어요. 먼저 인사해 보세요!')).toBeInTheDocument();
    expect(markRead).not.toHaveBeenCalled();
  });

  it('메시지 입력 후 전송 시 send.mutate(content) 호출', async () => {
    useMessagesMock.mockReturnValue({ isPending: false, isError: false, data: [] });
    renderChat();
    await userEvent.type(screen.getByPlaceholderText('메시지 입력'), '내일 봬요');
    await userEvent.click(screen.getByRole('button', { name: '전송' }));
    expect(send).toHaveBeenCalledWith('내일 봬요', expect.anything());
  });

  it('내용이 있으면 진입 시 읽음 처리', () => {
    useMessagesMock.mockReturnValue({
      isPending: false,
      isError: false,
      data: [message(1, 9, '안녕')],
    });
    renderChat();
    expect(markRead).toHaveBeenCalled();
  });
});
