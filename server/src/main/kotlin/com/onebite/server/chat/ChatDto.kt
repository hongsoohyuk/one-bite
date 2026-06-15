package com.onebite.server.chat

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SendMessageDto(
    @field:NotBlank
    @field:Size(max = 1000)
    val content: String,
)

data class ChatMessageResponse(
    val id: Long,
    val splitId: Long,
    val senderId: Long,
    val senderNickname: String,
    val senderProfileImageUrl: String?,
    val content: String,
    val createdAt: String,
) {
    companion object {
        fun from(m: ChatMessage) = ChatMessageResponse(
            id = m.id,
            splitId = m.splitId,
            senderId = m.sender.id,
            senderNickname = m.sender.nickname,
            senderProfileImageUrl = m.sender.profileImageUrl,
            content = m.content,
            createdAt = m.createdAt.toString(),
        )
    }
}

data class UnreadResponse(val count: Long)
