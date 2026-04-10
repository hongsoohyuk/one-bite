package com.onebite.server.auth

import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

@Component
class AppleClient(
    @Value("\${apple.client-id}") private val clientId: String
) {
    private val restTemplate = RestTemplate()
    private val keyCache = ConcurrentHashMap<String, PublicKey>()

    @Volatile
    private var lastKeyFetchTime = 0L
    private val keyTtlMs = 24 * 60 * 60 * 1000L // 24시간 캐시

    // Apple ID 토큰(JWT) 서명 검증 후 유저 정보 추출
    fun verifyAndGetUserInfo(idToken: String): SocialUserInfo {
        val publicKey = getApplePublicKey(idToken)

        val claims = Jwts.parser()
            .verifyWith(publicKey as java.security.interfaces.RSAPublicKey)
            .requireIssuer("https://appleid.apple.com")
            .requireAudience(clientId)
            .build()
            .parseSignedClaims(idToken)
            .payload

        val sub = claims.subject
            ?: throw RuntimeException("Apple ID 토큰에서 sub을 찾을 수 없습니다")
        val email = claims["email"] as? String

        return SocialUserInfo(
            id = sub,
            nickname = email?.substringBefore("@") ?: "한입유저",
            profileImageUrl = null  // Apple은 프로필 이미지를 제공하지 않음
        )
    }

    // JWT 헤더의 kid로 Apple 공개키 조회
    private fun getApplePublicKey(idToken: String): PublicKey {
        val headerJson = String(Base64.getUrlDecoder().decode(idToken.split(".")[0]))
        val kid = Regex("\"kid\"\\s*:\\s*\"([^\"]+)\"").find(headerJson)?.groupValues?.get(1)
            ?: throw RuntimeException("Apple ID 토큰에서 kid를 찾을 수 없습니다")

        refreshKeysIfNeeded()

        return keyCache[kid]
            ?: run {
                // 캐시 미스 — 키가 로테이션됐을 수 있으므로 강제 갱신
                fetchAndCacheKeys()
                keyCache[kid] ?: throw RuntimeException("Apple 공개키를 찾을 수 없습니다 (kid=$kid)")
            }
    }

    private fun refreshKeysIfNeeded() {
        if (keyCache.isEmpty() || System.currentTimeMillis() - lastKeyFetchTime > keyTtlMs) {
            fetchAndCacheKeys()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun fetchAndCacheKeys() {
        val response = restTemplate.getForObject(
            "https://appleid.apple.com/auth/keys",
            Map::class.java
        ) ?: throw RuntimeException("Apple 공개키 조회 실패")

        val keys = response["keys"] as? List<Map<String, Any>>
            ?: throw RuntimeException("Apple JWKS 파싱 실패")

        keyCache.clear()
        for (key in keys) {
            val kid = key["kid"] as String
            val n = key["n"] as String
            val e = key["e"] as String
            keyCache[kid] = buildRsaPublicKey(n, e)
        }
        lastKeyFetchTime = System.currentTimeMillis()
    }

    private fun buildRsaPublicKey(n: String, e: String): PublicKey {
        val modulus = BigInteger(1, Base64.getUrlDecoder().decode(n))
        val exponent = BigInteger(1, Base64.getUrlDecoder().decode(e))
        return KeyFactory.getInstance("RSA").generatePublic(RSAPublicKeySpec(modulus, exponent))
    }
}
