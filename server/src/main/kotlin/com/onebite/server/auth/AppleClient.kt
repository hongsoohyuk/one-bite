package com.onebite.server.auth

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Base64

@Component
class AppleClient(
    @Value("\${apple.client-id}") private val clientId: String
) {
    private val objectMapper = ObjectMapper()

    // Apple ID 토큰(JWT)에서 유저 정보 추출
    // Apple Sign In은 클라이언트가 직접 Apple로부터 id_token을 받아서 서버에 전달하는 방식
    fun verifyAndGetUserInfo(idToken: String): SocialUserInfo {
        // JWT payload 디코딩 (Base64)
        val parts = idToken.split(".")
        if (parts.size != 3) {
            throw RuntimeException("Apple ID 토큰 형식이 올바르지 않습니다")
        }

        val payload = String(Base64.getUrlDecoder().decode(parts[1]))
        val claims = objectMapper.readValue(payload, Map::class.java)

        // issuer 검증
        val issuer = claims["iss"] as? String
        if (issuer != "https://appleid.apple.com") {
            throw RuntimeException("Apple ID 토큰 issuer가 올바르지 않습니다")
        }

        // audience 검증
        val audience = claims["aud"] as? String
        if (audience != clientId) {
            throw RuntimeException("Apple ID 토큰 audience가 올바르지 않습니다")
        }

        val sub = claims["sub"] as? String
            ?: throw RuntimeException("Apple ID 토큰에서 sub을 찾을 수 없습니다")
        val email = claims["email"] as? String

        return SocialUserInfo(
            id = sub,
            nickname = email?.substringBefore("@") ?: "한입유저",
            profileImageUrl = null  // Apple은 프로필 이미지를 제공하지 않음
        )
    }
}
