package com.onebite.app.data.model

import kotlinx.serialization.Serializable

// AuthModels.kt - 인증 관련 데이터 모델
//
// API 명세(docs/api-spec.md)에 맞춘 요청/응답 타입 정의
// TypeScript에서 API 응답 타입을 정의하는 것과 동일:
//   interface LoginRequest { code: string }
//   interface LoginResponse { token: string; userId: number; ... }

@Serializable
data class KakaoLoginRequest(
    val code: String
)

@Serializable
data class NaverLoginRequest(
    val code: String,
    val state: String
)

@Serializable
data class GoogleLoginRequest(
    val code: String
)

@Serializable
data class AppleLoginRequest(
    val idToken: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val userId: Long,
    val nickname: String,
    val isNewUser: Boolean
)

@Serializable
data class UserProfile(
    val id: Long,
    val nickname: String,
    val profileImageUrl: String? = null,
    val createdAt: String? = null
)

@Serializable
data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String
)
