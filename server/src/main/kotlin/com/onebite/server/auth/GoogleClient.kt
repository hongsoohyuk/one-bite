package com.onebite.server.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Component
class GoogleClient(
    @Value("\${google.client-id}") private val clientId: String,
    @Value("\${google.client-secret}") private val clientSecret: String,
    @Value("\${google.redirect-uri}") private val redirectUri: String
) {
    private val restTemplate = RestTemplate()

    // Google 인가코드로 액세스 토큰 받기
    fun getAccessToken(authCode: String): String {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }

        val params = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", clientId)
            add("client_secret", clientSecret)
            add("redirect_uri", redirectUri)
            add("code", authCode)
        }

        val entity = HttpEntity(params, headers)
        val response = restTemplate.postForObject(
            "https://oauth2.googleapis.com/token",
            entity,
            Map::class.java
        )
        return response?.get("access_token") as? String
            ?: throw RuntimeException("Google 토큰 발급 실패")
    }

    // 액세스 토큰으로 유저 정보 가져오기
    fun getUserInfo(accessToken: String): SocialUserInfo {
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $accessToken")
        }
        val entity = HttpEntity<Any>(headers)

        val response = restTemplate.exchange(
            "https://www.googleapis.com/oauth2/v2/userinfo",
            HttpMethod.GET,
            entity,
            Map::class.java
        ).body ?: throw RuntimeException("Google 유저 정보 조회 실패")

        return SocialUserInfo(
            id = response["id"].toString(),
            nickname = response["name"] as? String ?: "한입유저",
            profileImageUrl = response["picture"] as? String
        )
    }
}
