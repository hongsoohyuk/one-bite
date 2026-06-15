package com.onebite.server.chat

import com.onebite.server.notification.NotificationService
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * 메시지 영속 커밋 후(AFTER_COMMIT) 비동기로:
 *   1. `/topic/chats/{splitId}` 로 실시간 브로드캐스트(접속 중인 멤버)
 *   2. 발신자 제외 멤버에게 FCM 푸시(미접속 멤버)
 *
 * @Transactional(readOnly): 새 스레드에서 LAZY sender 를 안전하게 로드하기 위함.
 */
@Component
class ChatEventListener(
    private val chatMessageRepository: ChatMessageRepository,
    private val messagingTemplate: SimpMessagingTemplate,
    private val notificationService: NotificationService,
) {
    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onChatMessage(e: ChatMessageCreatedEvent) {
        val message = chatMessageRepository.findById(e.messageId).orElse(null) ?: return
        messagingTemplate.convertAndSend("/topic/chats/${e.splitId}", ChatMessageResponse.from(message))
        notificationService.notifyChatMessage(e.splitId, e.senderId, message.content)
    }
}
