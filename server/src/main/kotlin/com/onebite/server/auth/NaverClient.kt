package com.onebite.server.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class NaverClient(
    @Value("\${naver.client-id}") private val clientId: String,
    @Value("\${naver.client-secret}") private val clientSecret: String
) {
    private val restTemplate = RestTemplate()

    // 네이버 인가코드로 액세스 토큰 받기
    fun getAccessToken(authCode: String, state: String): String {
        val url = "https://nid.naver.com/oauth2.0/token" +
            "?grant_type=authorization_code" +
            "&client_id=$clientId" +
            "&client_secret=$clientSecret" +
            "&code=$authCode" +
            "&state=$state"

        val response = restTemplate.postForObject(url, null, Map::class.java)
        return response?.get("access_token") as? String
            ?: throw RuntimeException("네이버 토큰 발급 실패")
    }

    // 액세스 토큰으로 유저 정보 가져오기
    fun getUserInfo(accessToken: String): SocialUserInfo {
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $accessToken")
        }
        val entity = HttpEntity<Any>(headers)

        val response = restTemplate.exchange(
            "https://openapi.naver.com/v1/nid/me",
            HttpMethod.GET,
            entity,
            Map::class.java
        ).body ?: throw RuntimeException("네이버 유저 정보 조회 실패")

        val profile = response["response"] as? Map<*, *>
            ?: throw RuntimeException("네이버 유저 프로필 파싱 실패")

        return SocialUserInfo(
            id = profile["id"].toString(),
            nickname = profile["nickname"] as? String ?: "한입유저",
            profileImageUrl = profile["profile_image"] as? String
        )
    }
}
