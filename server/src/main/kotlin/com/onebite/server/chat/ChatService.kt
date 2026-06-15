package com.onebite.server.chat

import com.onebite.server.split.ParticipantOutcome
import com.onebite.server.split.SplitParticipantRepository
import com.onebite.server.split.SplitRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class ChatService(
    private val chatMessageRepository: ChatMessageRepository,
    private val chatReadStateRepository: ChatReadStateRepository,
    private val splitRepository: SplitRepository,
    private val splitParticipantRepository: SplitParticipantRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    /** 주최자 또는 활성(JOINED/COMPLETED) 참여자만 채팅방 멤버 */
    @Transactional(readOnly = true)
    fun isMember(splitId: Long, userId: Long): Boolean {
        val split = splitRepository.findById(splitId).orElse(null) ?: return false
        if (split.author.id == userId) return true
        return splitParticipantRepository.findBySplitRequestId(splitId).any {
            it.user.id == userId &&
                (it.outcome == ParticipantOutcome.JOINED || it.outcome == ParticipantOutcome.COMPLETED)
        }
    }

    private fun requireMember(splitId: Long, userId: Long) {
        if (!splitRepository.existsById(splitId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "반띵을 찾을 수 없습니다: $splitId")
        }
        if (!isMember(splitId, userId)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "채팅방 멤버만 접근할 수 있습니다")
        }
    }

    /** 히스토리(최신순). before 가 있으면 그 id 이전(더 과거) 메시지. */
    @Transactional(readOnly = true)
    fun listMessages(splitId: Long, userId: Long, before: Long?, size: Int): List<ChatMessageResponse> {
        requireMember(splitId, userId)
        val pageable = PageRequest.of(0, size.coerceIn(1, 100))
        val rows = if (before != null)
            chatMessageRepository.findBySplitIdAndIdLessThanOrderByIdDesc(splitId, before, pageable)
        else
            chatMessageRepository.findBySplitIdOrderByIdDesc(splitId, pageable)
        return rows.map { ChatMessageResponse.from(it) }
    }

    @Transactional
    fun sendMessage(splitId: Long, userId: Long, content: String): ChatMessageResponse {
        requireMember(splitId, userId)
        val split = splitRepository.findById(splitId).get()
        // sender 는 split.author 또는 참여자 → 둘 중 하나로 User 참조 확보 (별도 조회 없이)
        val sender = if (split.author.id == userId) split.author
        else splitParticipantRepository.findBySplitRequestId(splitId).first { it.user.id == userId }.user

        val saved = chatMessageRepository.save(ChatMessage(splitId = splitId, sender = sender, content = content))
        eventPublisher.publishEvent(ChatMessageCreatedEvent(splitId, saved.id, userId))
        return ChatMessageResponse.from(saved)
    }

    @Transactional
    fun markRead(splitId: Long, userId: Long) {
        requireMember(splitId, userId)
        val state = chatReadStateRepository.findBySplitIdAndUserId(splitId, userId)
        if (state == null) {
            chatReadStateRepository.save(ChatReadState(splitId = splitId, userId = userId))
        } else {
            state.lastReadAt = LocalDateTime.now()
            chatReadStateRepository.save(state)
        }
    }

    @Transactional(readOnly = true)
    fun unreadCount(splitId: Long, userId: Long): Long {
        requireMember(splitId, userId)
        val since = chatReadStateRepository.findBySplitIdAndUserId(splitId, userId)?.lastReadAt
            ?: LocalDateTime.of(1970, 1, 1, 0, 0)
        return chatMessageRepository.countBySplitIdAndCreatedAtAfterAndSenderIdNot(splitId, since, userId)
    }
}
