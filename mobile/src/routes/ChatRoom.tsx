import { useEffect, useState, type FormEvent } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { AppBar } from '../shared/components/AppBar';
import { LoadingState } from '../shared/components/states/LoadingState';
import { ErrorState } from '../shared/components/states/ErrorState';
import { useSplit } from '../features/splits/queries';
import {
  useChatMessages,
  useChatSubscription,
  useMarkChatRead,
  useSendChatMessage,
} from '../features/chat/queries';
import { useAuthStore } from '../shared/stores/authStore';
import { cn } from '../shared/lib/cn';
import { formatRelativeTime } from '../shared/lib/format';

export function ChatRoom() {
  const { id } = useParams();
  const splitId = Number(id);
  const navigate = useNavigate();
  const { t } = useTranslation();
  const myId = useAuthStore((s) => s.user?.id);

  const split = useSplit(splitId);
  const messages = useChatMessages(splitId);
  const send = useSendChatMessage(splitId);
  const markRead = useMarkChatRead(splitId);
  const connected = useChatSubscription(splitId);
  const [draft, setDraft] = useState('');

  // 진입/메시지 도착 시 읽음 처리
  const count = messages.data?.length ?? 0;
  useEffect(() => {
    if (count > 0) markRead.mutate();
    // markRead 는 안정 참조가 아니므로 의존성에서 제외 (count 변화 때만 실행)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [count]);

  function submit(e: FormEvent) {
    e.preventDefault();
    const content = draft.trim();
    if (!content || send.isPending) return;
    send.mutate(content, { onSuccess: () => setDraft('') });
  }

  const title = split.data?.productName ?? t('chat.title');

  return (
    <div className="flex h-screen flex-col bg-white dark:bg-gray-950">
      <AppBar
        title={title}
        onBack={() => navigate(-1)}
        actions={
          <span className="pr-2 text-meta text-gray-400">
            {connected ? t('chat.connected') : t('chat.connecting')}
          </span>
        }
      />

      {messages.isPending ? (
        <LoadingState />
      ) : messages.isError ? (
        <ErrorState message={t('chat.loadError')} onRetry={() => void messages.refetch()} />
      ) : (
        // newest-first 배열 + flex-col-reverse → 최신이 하단, 진입 시 하단부터 보임
        <div className="flex flex-1 flex-col-reverse overflow-y-auto px-4 py-3">
          {messages.data.length === 0 ? (
            <p className="py-10 text-center text-caption text-gray-400">{t('chat.empty')}</p>
          ) : (
            messages.data.map((m) => {
              const mine = m.senderId === myId;
              return (
                <div
                  key={m.id}
                  className={cn('mb-2 flex flex-col', mine ? 'items-end' : 'items-start')}
                >
                  {!mine && (
                    <span className="mb-0.5 text-meta text-gray-400">{m.senderNickname}</span>
                  )}
                  <div
                    className={cn(
                      'max-w-[75%] whitespace-pre-wrap break-words rounded-2xl px-3.5 py-2 text-body',
                      mine
                        ? 'bg-brand text-white dark:bg-brand-dark-adj'
                        : 'bg-gray-100 text-gray-900 dark:bg-gray-800 dark:text-gray-50',
                    )}
                  >
                    {m.content}
                  </div>
                  <span className="mt-0.5 text-meta text-gray-400">
                    {formatRelativeTime(m.createdAt)}
                  </span>
                </div>
              );
            })
          )}
        </div>
      )}

      <form
        onSubmit={submit}
        className="flex items-center gap-2 border-t border-gray-200 p-3 dark:border-gray-700"
      >
        <input
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          placeholder={t('chat.inputPlaceholder')}
          aria-label={t('chat.inputPlaceholder')}
          className="h-11 flex-1 rounded-pill border border-gray-200 bg-white px-4 text-body text-gray-900 outline-none placeholder:text-gray-400 focus:border-brand dark:border-gray-700 dark:bg-gray-900 dark:text-gray-50"
        />
        <button
          type="submit"
          disabled={!draft.trim() || send.isPending}
          className="h-11 shrink-0 rounded-pill bg-brand px-5 text-body font-medium text-white disabled:opacity-40 dark:bg-brand-dark-adj"
        >
          {t('chat.send')}
        </button>
      </form>
    </div>
  );
}
