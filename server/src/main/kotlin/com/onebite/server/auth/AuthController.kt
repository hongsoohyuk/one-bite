package com.onebite.server.auth

import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    // GET /api/auth/callback/{provider} — OAuth redirect relay (iOS 웹 OAuth용)
    // 카카오 등에서 리다이렉트 → 커스텀 스킴으로 앱에 전달
    @GetMapping("/callback/{provider}")
    fun oauthCallback(
        @PathVariable provider: String,
        @RequestParam code: String?,
        @RequestParam state: String?,
        response: HttpServletResponse
    ) {
        val scheme = "com.onebite.app"
        val params = buildList {
            code?.let { add("code=$it") }
            state?.let { add("state=$it") }
        }.joinToString("&")
        response.sendRedirect("$scheme://oauth/$provider?$params")
    }

    // POST /api/auth/kakao — 카카오 로그인 (인가코드 or 액세스토큰)
    @PostMapping("/kakao")
    fun kakaoLogin(@RequestBody request: KakaoLoginRequest): AuthResponse =
        if (request.accessToken != null)
            authService.kakaoLoginWithToken(request.accessToken)
        else
            authService.kakaoLogin(request.code!!)

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
    val code: String? = null,        // iOS: 인가 코드
    val accessToken: String? = null  // Android: SDK에서 받은 액세스 토큰
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
