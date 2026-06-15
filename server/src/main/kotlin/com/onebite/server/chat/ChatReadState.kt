package com.onebite.server.chat

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 방(split) 단위 유저별 마지막 읽음 시각. 안읽음 수 계산에 사용.
 */
@Entity
@Table(
    name = "chat_read_states",
    uniqueConstraints = [UniqueConstraint(columnNames = ["split_id", "user_id"])]
)
class ChatReadState(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "split_id", nullable = false)
    val splitId: Long,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "last_read_at", nullable = false)
    var lastReadAt: LocalDateTime = LocalDateTime.now(),
)
