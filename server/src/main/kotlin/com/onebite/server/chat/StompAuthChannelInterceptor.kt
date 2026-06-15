package com.onebite.server.chat

import com.onebite.server.auth.JwtProvider
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageDeliveryException
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Component

/**
 * STOMP 인증/인가.
 * - CONNECT: native 헤더 `Authorization: Bearer <jwt>` 검증 → Principal(userId) 세션에 저장
 * - SUBSCRIBE: `/topic/chats/{splitId}` 구독 시 해당 split 채팅방 멤버인지 검사 (비멤버 차단)
 */
@Component
class StompAuthChannelInterceptor(
    private val jwtProvider: JwtProvider,
    private val chatService: ChatService,
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        // wrap() 은 read-only 복사본이라 user 설정이 전파되지 않음 → getAccessor 로 가변 accessor 획득
        val accessor = StompHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
            ?: return message
        when (accessor.command) {
            StompCommand.CONNECT -> {
                val token = accessor.getFirstNativeHeader("Authorization")
                    ?.removePrefix("Bearer ")?.trim()
                if (token.isNullOrBlank() || !jwtProvider.isValid(token)) {
                    throw MessageDeliveryException(message, "STOMP 인증 실패")
                }
                accessor.user = StompPrincipal(jwtProvider.getUserId(token))
            }

            StompCommand.SUBSCRIBE -> {
                val principal = accessor.user as? StompPrincipal
                    ?: throw MessageDeliveryException(message, "인증이 필요합니다")
                val splitId = topicSplitId(accessor.destination)
                if (splitId != null && !chatService.isMember(splitId, principal.userId)) {
                    throw MessageDeliveryException(message, "채팅방 멤버가 아닙니다")
                }
            }

            else -> {}
        }
        return message
    }

    // "/topic/chats/{splitId}" → splitId. 형식이 다르면 null(검사 생략).
    private fun topicSplitId(destination: String?): Long? {
        val prefix = "/topic/chats/"
        val dest = destination ?: return null
        if (!dest.startsWith(prefix)) return null
        return dest.removePrefix(prefix).toLongOrNull()
    }
}
