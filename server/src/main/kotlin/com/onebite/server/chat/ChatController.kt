package com.onebite.server.chat

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/splits/{splitId}/chat")
class ChatController(
    private val chatService: ChatService,
) {
    // 메시지 히스토리(최신순). before=마지막으로 받은 가장 오래된 메시지 id (더 과거 로딩)
    @GetMapping("/messages")
    fun list(
        @PathVariable splitId: Long,
        @RequestParam(required = false) before: Long?,
        @RequestParam(defaultValue = "30") size: Int,
        authentication: Authentication,
    ): List<ChatMessageResponse> {
        val userId = authentication.principal as Long
        return chatService.listMessages(splitId, userId, before, size)
    }

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    fun send(
        @PathVariable splitId: Long,
        @Valid @RequestBody dto: SendMessageDto,
        authentication: Authentication,
    ): ChatMessageResponse {
        val userId = authentication.principal as Long
        return chatService.sendMessage(splitId, userId, dto.content)
    }

    @PostMapping("/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun read(@PathVariable splitId: Long, authentication: Authentication) {
        val userId = authentication.principal as Long
        chatService.markRead(splitId, userId)
    }

    @GetMapping("/unread")
    fun unread(@PathVariable splitId: Long, authentication: Authentication): UnreadResponse {
        val userId = authentication.principal as Long
        return UnreadResponse(chatService.unreadCount(splitId, userId))
    }
}
