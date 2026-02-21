package com.onebite.server.auth

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    // POST /api/auth/kakao — 카카오 인가코드로 로그인
    @PostMapping("/kakao")
    fun kakaoLogin(@RequestBody request: KakaoLoginRequest): AuthResponse =
        authService.kakaoLogin(request.code)

    // POST /api/auth/naver — 네이버 인가코드로 로그인
    @PostMapping("/naver")
    fun naverLogin(@RequestBody request: NaverLoginRequest): AuthResponse =
        authService.naverLogin(request.code, request.state)

    // POST /api/auth/google — Google 인가코드로 로그인
    @PostMapping("/google")
    fun googleLogin(@RequestBody request: GoogleLoginRequest): AuthResponse =
        authService.googleLogin(request.code)

    // POST /api/auth/apple — Apple ID 토큰으로 로그인
    @PostMapping("/apple")
    fun appleLogin(@RequestBody request: AppleLoginRequest): AuthResponse =
        authService.appleLogin(request.idToken)
}

data class KakaoLoginRequest(
    val code: String  // 카카오 인가 코드
)

data class NaverLoginRequest(
    val code: String,  // 네이버 인가 코드
    val state: String  // CSRF 방지 state 파라미터
)

data class GoogleLoginRequest(
    val code: String  // Google 인가 코드
)

data class AppleLoginRequest(
    val idToken: String  // Apple ID 토큰 (JWT)
)
