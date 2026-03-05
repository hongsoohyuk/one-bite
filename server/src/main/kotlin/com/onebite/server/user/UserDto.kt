package com.onebite.server.user

data class UserResponse(
    val id: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val createdAt: String
) {
    companion object {
        fun from(user: User) = UserResponse(
            id = user.id,
            nickname = user.nickname,
            profileImageUrl = user.profileImageUrl,
            createdAt = user.createdAt.toString()
        )
    }
}

data class UpdateUserDto(
    val nickname: String? = null
)
