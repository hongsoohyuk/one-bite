package com.onebite.server.ratelimit

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TokenBucketTest {

    private val oneMinuteNanos = 60_000_000_000L

    @Test
    fun `capacity 만큼 소비하면 그 다음은 차단된다`() {
        val bucket = TokenBucket(capacity = 3.0, refillPerMinute = 3.0, nowNanos = 0L)
        assertTrue(bucket.tryConsume(0L))
        assertTrue(bucket.tryConsume(0L))
        assertTrue(bucket.tryConsume(0L))
        assertFalse(bucket.tryConsume(0L)) // 토큰 소진
    }

    @Test
    fun `시간이 지나면 일정 속도로 보충된다`() {
        val bucket = TokenBucket(capacity = 2.0, refillPerMinute = 2.0, nowNanos = 0L)
        assertTrue(bucket.tryConsume(0L))
        assertTrue(bucket.tryConsume(0L))
        assertFalse(bucket.tryConsume(0L))

        // 분당 2개 → 30초면 1개 보충
        assertTrue(bucket.tryConsume(oneMinuteNanos / 2))
        assertFalse(bucket.tryConsume(oneMinuteNanos / 2))
    }

    @Test
    fun `보충은 capacity 를 넘지 않는다`() {
        val bucket = TokenBucket(capacity = 2.0, refillPerMinute = 60.0, nowNanos = 0L)
        // 한참 뒤에도 최대 2개까지만
        assertTrue(bucket.tryConsume(oneMinuteNanos * 10))
        assertTrue(bucket.tryConsume(oneMinuteNanos * 10))
        assertFalse(bucket.tryConsume(oneMinuteNanos * 10))
    }

    @Test
    fun `소진 시 다음 토큰까지 남은 초를 알려준다`() {
        val bucket = TokenBucket(capacity = 1.0, refillPerMinute = 60.0, nowNanos = 0L) // 초당 1개
        assertEquals(0L, bucket.secondsUntilNextToken(0L)) // 아직 토큰 있음
        assertTrue(bucket.tryConsume(0L))
        assertEquals(1L, bucket.secondsUntilNextToken(0L)) // 소진 직후 → 약 1초 대기
    }

    @Test
    fun `가득 찬 버킷은 isFull true`() {
        val bucket = TokenBucket(capacity = 2.0, refillPerMinute = 2.0, nowNanos = 0L)
        assertTrue(bucket.isFull(0L))
        bucket.tryConsume(0L)
        assertFalse(bucket.isFull(0L))
    }
}
