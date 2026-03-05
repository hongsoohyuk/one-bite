package com.onebite.server.user

import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    // GET /api/users/me
    @GetMapping("/me")
    fun getMyProfile(authentication: Authentication): UserResponse {
        val userId = authentication.principal as Long
        return userService.getProfile(userId)
    }

    // PATCH /api/users/me
    @PatchMapping("/me")
    fun updateMyProfile(
        @RequestBody dto: UpdateUserDto,
        authentication: Authentication
    ): UserResponse {
        val userId = authentication.principal as Long
        return userService.updateProfile(userId, dto)
    }
}
