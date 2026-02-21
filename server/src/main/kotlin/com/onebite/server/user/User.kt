package com.onebite.server.user

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true)
    val kakaoId: String,

    var nickname: String,
    var profileImageUrl: String? = null,

    val createdAt: LocalDateTime = LocalDateTime.now()
)
