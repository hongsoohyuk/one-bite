package com.onebite.server.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByKakaoId(kakaoId: String): User?
}
