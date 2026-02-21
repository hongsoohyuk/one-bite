package com.onebite.server.auth

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    // POST /api/auth/kakao — 모바일에서 카카오 인가코드를 보내면 JWT 반환
    @PostMapping("/kakao")
    fun kakaoLogin(@RequestBody request: KakaoLoginRequest): AuthResponse =
        authService.kakaoLogin(request.code)
}

data class KakaoLoginRequest(
    val code: String  // 카카오 인가 코드
)
