package com.onebite.server.auth

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JwtProviderTest {

    private val secret = "test-secret-key-must-be-at-least-32-bytes-long!!"
    private val expirationMs = 3600000L
    private val jwtProvider = JwtProvider(secret, expirationMs)

    @Test
    fun `토큰 생성 후 userId 추출`() {
        val token = jwtProvider.generateToken(42L)
        assertEquals(42L, jwtProvider.getUserId(token))
    }

    @Test
    fun `유효한 토큰 검증 성공`() {
        val token = jwtProvider.generateToken(1L)
        assertTrue(jwtProvider.isValid(token))
    }

    @Test
    fun `잘못된 토큰 검증 실패`() {
        assertFalse(jwtProvider.isValid("invalid.token.here"))
    }

    @Test
    fun `만료된 토큰 검증 실패`() {
        val expiredProvider = JwtProvider(secret, -1000L) // 이미 만료된 토큰
        val token = expiredProvider.generateToken(1L)
        assertFalse(jwtProvider.isValid(token))
    }

    @Test
    fun `다른 시크릿으로 서명된 토큰 검증 실패`() {
        val otherProvider = JwtProvider("another-secret-key-at-least-32-bytes-long!!", expirationMs)
        val token = otherProvider.generateToken(1L)
        assertFalse(jwtProvider.isValid(token))
    }
}
