package com.onebite.server.chat

import com.onebite.server.auth.JwtProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageDeliveryException
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.MessageBuilder
import java.security.Principal

class StompAuthChannelInterceptorTest {

    private val jwtProvider = JwtProvider("test-secret-test-secret-test-secret-1234567890", 3_600_000)
    private val chatService = Mockito.mock(ChatService::class.java)
    private val interceptor = StompAuthChannelInterceptor(jwtProvider, chatService)
    private val channel = Mockito.mock(MessageChannel::class.java)

    private fun frame(
        command: StompCommand,
        token: String? = null,
        destination: String? = null,
        user: Principal? = null,
    ): Message<ByteArray> {
        val accessor = StompHeaderAccessor.create(command)
        accessor.setLeaveMutable(true)
        if (token != null) accessor.setNativeHeader("Authorization", "Bearer $token")
        if (destination != null) accessor.destination = destination
        if (user != null) accessor.user = user
        return MessageBuilder.createMessage(ByteArray(0), accessor.messageHeaders)
    }

    @Test
    fun `CONNECT 유효 토큰이면 Principal 을 세팅`() {
        val token = jwtProvider.generateToken(42)
        val msg = frame(StompCommand.CONNECT, token = token)
        interceptor.preSend(msg, channel)
        val principal = StompHeaderAccessor.getAccessor(msg, StompHeaderAccessor::class.java)!!.user
        assertEquals(42L, (principal as StompPrincipal).userId)
    }

    @Test
    fun `CONNECT 토큰 없으면 거부`() {
        val msg = frame(StompCommand.CONNECT)
        assertThrows(MessageDeliveryException::class.java) { interceptor.preSend(msg, channel) }
    }

    @Test
    fun `CONNECT 잘못된 토큰이면 거부`() {
        val msg = frame(StompCommand.CONNECT, token = "garbage.token.value")
        assertThrows(MessageDeliveryException::class.java) { interceptor.preSend(msg, channel) }
    }

    @Test
    fun `SUBSCRIBE 멤버면 통과`() {
        Mockito.`when`(chatService.isMember(7L, 42L)).thenReturn(true)
        val msg = frame(StompCommand.SUBSCRIBE, destination = "/topic/chats/7", user = StompPrincipal(42))
        interceptor.preSend(msg, channel) // no throw
    }

    @Test
    fun `SUBSCRIBE 비멤버면 거부`() {
        Mockito.`when`(chatService.isMember(7L, 42L)).thenReturn(false)
        val msg = frame(StompCommand.SUBSCRIBE, destination = "/topic/chats/7", user = StompPrincipal(42))
        assertThrows(MessageDeliveryException::class.java) { interceptor.preSend(msg, channel) }
    }

    @Test
    fun `SUBSCRIBE 인증 없으면 거부`() {
        val msg = frame(StompCommand.SUBSCRIBE, destination = "/topic/chats/7")
        assertThrows(MessageDeliveryException::class.java) { interceptor.preSend(msg, channel) }
    }

    @Test
    fun `채팅 토픽이 아니면 멤버십 검사 생략`() {
        val msg = frame(StompCommand.SUBSCRIBE, destination = "/topic/other", user = StompPrincipal(42))
        interceptor.preSend(msg, channel) // no throw, no membership check
    }
}
