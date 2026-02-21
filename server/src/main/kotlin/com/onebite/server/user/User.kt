package com.onebite.server.user

import jakarta.persistence.*
import java.time.LocalDateTime

enum class AuthProvider {
    KAKAO, NAVER, GOOGLE, APPLE
}

@Entity
@Table(name = "users", uniqueConstraints = [
    UniqueConstraint(columnNames = ["provider", "providerId"])
])
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Enumerated(EnumType.STRING)
    val provider: AuthProvider,

    val providerId: String,

    var nickname: String,
    var profileImageUrl: String? = null,

    val createdAt: LocalDateTime = LocalDateTime.now()
)
