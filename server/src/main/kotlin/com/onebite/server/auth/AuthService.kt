package com.onebite.server.auth

import com.onebite.server.user.AuthProvider
import com.onebite.server.user.User
import com.onebite.server.user.UserRepository
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val kakaoClient: KakaoClient,
    private val naverClient: NaverClient,
    private val googleClient: GoogleClient,
    private val appleClient: AppleClient,
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider
) {
    // 카카오 로그인 (인가코드 → 토큰 교환 → userInfo)
    fun kakaoLogin(authCode: String): AuthResponse {
        val kakaoToken = kakaoClient.getAccessToken(authCode)
        val userInfo = kakaoClient.getUserInfo(kakaoToken)
        return loginOrRegister(AuthProvider.KAKAO, userInfo)
    }

    // 카카오 로그인 (액세스토큰 → 바로 userInfo)
    fun kakaoLoginWithToken(accessToken: String): AuthResponse {
        val userInfo = kakaoClient.getUserInfo(accessToken)
        return loginOrRegister(AuthProvider.KAKAO, userInfo)
    }

    // 네이버 로그인
    fun naverLogin(authCode: String, state: String): AuthResponse {
        val naverToken = naverClient.getAccessToken(authCode, state)
        val userInfo = naverClient.getUserInfo(naverToken)
        return loginOrRegister(AuthProvider.NAVER, userInfo)
    }

    // Google 로그인 (인가코드 → 토큰 교환 → userInfo)
    fun googleLogin(authCode: String): AuthResponse {
        val googleToken = googleClient.getAccessToken(authCode)
        val userInfo = googleClient.getUserInfo(googleToken)
        return loginOrRegister(AuthProvider.GOOGLE, userInfo)
    }

    // Google 로그인 (ID 토큰 → 검증 → userInfo)
    fun googleLoginWithToken(idToken: String): AuthResponse {
        val userInfo = googleClient.verifyIdTokenAndGetUserInfo(idToken)
        return loginOrRegister(AuthProvider.GOOGLE, userInfo)
    }

    // Apple 로그인
    fun appleLogin(idToken: String): AuthResponse {
        val userInfo = appleClient.verifyAndGetUserInfo(idToken)
        return loginOrRegister(AuthProvider.APPLE, userInfo)
    }

    // 공통: 기존 유저 조회 or 신규 가입 → JWT 발급
    private fun loginOrRegister(provider: AuthProvider, userInfo: SocialUserInfo): AuthResponse {
        var isNewUser = false
        val user = userRepository.findByProviderAndProviderId(provider, userInfo.id)
            ?: run {
                isNewUser = true
                userRepository.save(
                    User(
                        provider = provider,
                        providerId = userInfo.id,
                        nickname = userInfo.nickname,
                        profileImageUrl = userInfo.profileImageUrl
                    )
                )
            }

        val token = jwtProvider.generateToken(user.id)

        return AuthResponse(
            token = token,
            userId = user.id,
            nickname = user.nickname,
            isNewUser = isNewUser
        )
    }
}

data class AuthResponse(
    val token: String,
    val userId: Long,
    val nickname: String,
    val isNewUser: Boolean
)
