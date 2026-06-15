package com.onebite.server.chat

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    // 최신순 히스토리 (커서 없음: 가장 최근 size 개)
    fun findBySplitIdOrderByIdDesc(splitId: Long, pageable: Pageable): List<ChatMessage>

    // 커서(before) 이전(더 과거)의 메시지 최신순
    fun findBySplitIdAndIdLessThanOrderByIdDesc(splitId: Long, beforeId: Long, pageable: Pageable): List<ChatMessage>

    // 내 마지막 읽음 이후 + 내가 보낸 게 아닌 메시지 수 (안읽음)
    fun countBySplitIdAndCreatedAtAfterAndSenderIdNot(splitId: Long, after: LocalDateTime, senderId: Long): Long
}

interface ChatReadStateRepository : JpaRepository<ChatReadState, Long> {
    fun findBySplitIdAndUserId(splitId: Long, userId: Long): ChatReadState?
}
