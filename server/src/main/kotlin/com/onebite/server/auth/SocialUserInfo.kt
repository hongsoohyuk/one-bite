package com.onebite.server.auth

// 모든 소셜 로그인 provider가 공통으로 반환하는 유저 정보
data class SocialUserInfo(
    val id: String,
    val nickname: String,
    val profileImageUrl: String?
)
