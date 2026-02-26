package com.onebite.server.split

import com.onebite.server.user.User
import jakarta.persistence.*
import java.time.LocalDateTime

// ── Entity: DB 테이블과 매핑 (Prisma의 model과 같은 역할) ──
@Entity
@Table(name = "split_requests")
class SplitRequest(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    val author: User,

    val productName: String,
    val totalPrice: Int,
    val totalQty: Int,
    val splitCount: Int,
    val imageUrl: String? = null,

    // 위치 정보
    val latitude: Double,
    val longitude: Double,
    val address: String,

    @Enumerated(EnumType.STRING)
    var status: SplitStatus = SplitStatus.WAITING,

    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class SplitStatus {
    WAITING,     // 나눌 사람 대기중
    MATCHED,     // 매칭됨
    COMPLETED,   // 거래 완료
    CANCELLED    // 취소
}
