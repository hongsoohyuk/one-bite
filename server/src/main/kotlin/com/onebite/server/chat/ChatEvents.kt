package com.onebite.server.chat

/** 채팅 메시지 생성 → AFTER_COMMIT 으로 STOMP 브로드캐스트 + FCM 푸시 트리거 */
data class ChatMessageCreatedEvent(val splitId: Long, val messageId: Long, val senderId: Long)
