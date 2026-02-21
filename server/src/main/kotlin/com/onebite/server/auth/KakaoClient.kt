package com.onebite.server.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

// 카카오 API 연동 클라이언트
@Component
class KakaoClient(
    @Value("\${kakao.client-id}") private val clientId: String,
    @Value("\${kakao.redirect-uri}") private val redirectUri: String
) {
    private val restTemplate = RestTemplate()

    // 카카오 인가코드로 액세스 토큰 받기
    fun getAccessToken(authCode: String): String {
        val url = "https://kauth.kakao.com/oauth/token" +
            "?grant_type=authorization_code" +
            "&client_id=$clientId" +
            "&redirect_uri=$redirectUri" +
            "&code=$authCode"

        val response = restTemplate.postForObject(url, null, Map::class.java)
        return response?.get("access_token") as? String
            ?: throw RuntimeException("카카오 토큰 발급 실패")
    }

    // 액세스 토큰으로 유저 정보 가져오기
    fun getUserInfo(accessToken: String): KakaoUserInfo {
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $accessToken")
        }
        val entity = HttpEntity<Any>(headers)

        val response = restTemplate.exchange(
            "https://kapi.kakao.com/v2/user/me",
            HttpMethod.GET,
            entity,
            Map::class.java
        ).body ?: throw RuntimeException("카카오 유저 정보 조회 실패")

        val kakaoId = response["id"].toString()
        val properties = response["properties"] as? Map<*, *>
        val nickname = properties?.get("nickname") as? String ?: "한입유저"
        val profileImage = properties?.get("profile_image") as? String

        return KakaoUserInfo(kakaoId, nickname, profileImage)
    }
}

data class KakaoUserInfo(
    val kakaoId: String,
    val nickname: String,
    val profileImageUrl: String?
)
