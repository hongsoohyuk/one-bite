package com.onebite.server.ratelimit

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RateLimiterTest {

    /** 테스트용 가변 시계. */
    private class FakeClock(var nanos: Long = 0L) : () -> Long {
        override fun invoke(): Long = nanos
    }

    @Test
    fun `한도까지 허용하고 초과분은 차단한다`() {
        val limiter = RateLimiter(capacity = 3.0, refillPerMinute = 3.0, maxTrackedClients = 100, clock = FakeClock())
        repeat(3) { assertTrue(limiter.check("auth:1.1.1.1").allowed) }
        val denied = limiter.check("auth:1.1.1.1")
        assertFalse(denied.allowed)
        assertTrue(denied.retryAfterSeconds >= 1)
    }

    @Test
    fun `키가 다르면 버킷이 독립적이다`() {
        val limiter = RateLimiter(capacity = 1.0, refillPerMinute = 1.0, maxTrackedClients = 100, clock = FakeClock())
        assertTrue(limiter.check("auth:1.1.1.1").allowed)
        assertFalse(limiter.check("auth:1.1.1.1").allowed)
        // 다른 IP 는 영향 없음
        assertTrue(limiter.check("auth:2.2.2.2").allowed)
    }

    @Test
    fun `시간이 지나면 다시 허용된다`() {
        val clock = FakeClock()
        val limiter = RateLimiter(capacity = 1.0, refillPerMinute = 60.0, maxTrackedClients = 100, clock = clock)
        assertTrue(limiter.check("k").allowed)
        assertFalse(limiter.check("k").allowed)
        clock.nanos = 1_000_000_000L // 1초 경과 → 초당 1개 보충
        assertTrue(limiter.check("k").allowed)
    }

    @Test
    fun `최대 추적 키 초과 시 idle 버킷을 청소한다`() {
        val clock = FakeClock()
        val limiter = RateLimiter(capacity = 5.0, refillPerMinute = 300.0, maxTrackedClients = 2, clock = clock)
        // a, b, c 를 한 번씩 사용(각 4토큰) → size=3 > max=2
        limiter.check("a")
        limiter.check("b")
        limiter.check("c")
        // 시간 경과로 a, b, c 가 capacity 까지 보충(=idle/full)
        clock.nanos = 60_000_000_000L
        // 새 키 진입 시 size>max → sweep 으로 가득 찬 idle 버킷 제거
        limiter.check("d")
        assertTrue(limiter.trackedCount() <= 2, "idle 버킷 청소로 추적 키가 줄어야 함(=${limiter.trackedCount()})")
    }
}
