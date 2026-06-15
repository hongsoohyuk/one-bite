package com.onebite.server.chat

import com.onebite.server.user.User
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 채팅 메시지. 방은 split 과 1:1 이라 별도 room 테이블 없이 splitId 를 방 키로 쓴다.
 */
@Entity
@Table(name = "chat_messages")
class ChatMessage(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "split_id", nullable = false)
    val splitId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    val sender: User,

    @Column(nullable = false, length = 1000)
    val content: String,

    val createdAt: LocalDateTime = LocalDateTime.now(),
)
