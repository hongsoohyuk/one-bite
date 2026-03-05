package com.onebite.server.user

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun getProfile(userId: Long): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다: $userId") }
        return UserResponse.from(user)
    }

    fun updateProfile(userId: Long, dto: UpdateUserDto): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다: $userId") }

        dto.nickname?.let { user.nickname = it }

        val saved = userRepository.save(user)
        return UserResponse.from(saved)
    }
}
