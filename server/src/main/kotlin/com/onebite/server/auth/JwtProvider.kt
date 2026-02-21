package com.onebite.server.auth

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration}") private val expirationMs: Long
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    // JWT 토큰 생성
    fun generateToken(userId: Long): String =
        Jwts.builder()
            .subject(userId.toString())
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(key)
            .compact()

    // 토큰에서 userId 추출
    fun getUserId(token: String): Long =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
            .toLong()

    // 토큰 유효성 검증
    fun isValid(token: String): Boolean =
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
}
