package com.onebite.server.auth

import com.onebite.server.user.User
import com.onebite.server.user.UserRepository
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val kakaoClient: KakaoClient,
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider
) {
    // 카카오 로그인 전체 플로우
    fun kakaoLogin(authCode: String): AuthResponse {
        // 1. 카카오에서 액세스 토큰 받기
        val kakaoToken = kakaoClient.getAccessToken(authCode)

        // 2. 토큰으로 유저 정보 가져오기
        val kakaoUser = kakaoClient.getUserInfo(kakaoToken)

        // 3. 기존 유저 조회 or 신규 가입
        val user = userRepository.findByKakaoId(kakaoUser.kakaoId)
            ?: userRepository.save(
                User(
                    kakaoId = kakaoUser.kakaoId,
                    nickname = kakaoUser.nickname,
                    profileImageUrl = kakaoUser.profileImageUrl
                )
            )

        // 4. JWT 토큰 발급
        val token = jwtProvider.generateToken(user.id)

        return AuthResponse(
            token = token,
            userId = user.id,
            nickname = user.nickname,
            isNewUser = user.createdAt == user.createdAt // 첫 로그인 판별은 추후 개선
        )
    }
}

data class AuthResponse(
    val token: String,
    val userId: Long,
    val nickname: String,
    val isNewUser: Boolean
)
