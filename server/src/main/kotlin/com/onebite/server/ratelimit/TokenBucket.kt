package com.onebite.server.ratelimit

import kotlin.math.ceil

/**
 * 단순 토큰 버킷. capacity 만큼 토큰을 담고, 분당 refillPerMinute 개씩 일정 속도로 보충된다.
 *
 * 스레드 안전(메서드 단위 synchronized). 시계는 나노초 단위로 외부에서 주입받아(테스트 결정성)
 * System.nanoTime 의존을 끊는다.
 */
class TokenBucket(
    private val capacity: Double,
    refillPerMinute: Double,
    nowNanos: Long,
) {
    private val refillPerNano: Double = (refillPerMinute / 60.0 / 1_000_000_000.0).coerceAtLeast(Double.MIN_VALUE)
    private var tokens: Double = capacity
    private var lastRefillNanos: Long = nowNanos

    /** 토큰 1개 소비 시도. 성공하면 true(허용), 부족하면 false(차단). */
    @Synchronized
    fun tryConsume(nowNanos: Long): Boolean {
        refill(nowNanos)
        if (tokens >= 1.0) {
            tokens -= 1.0
            return true
        }
        return false
    }

    /** 보충 반영 후 버킷이 가득 찼는지 — idle(사용 안 함) 판단용, 메모리 청소 대상 선별. */
    @Synchronized
    fun isFull(nowNanos: Long): Boolean {
        refill(nowNanos)
        return tokens >= capacity
    }

    /** 다음 토큰이 찰 때까지 남은 시간(초, 올림). Retry-After 헤더용. 최소 1초. */
    @Synchronized
    fun secondsUntilNextToken(nowNanos: Long): Long {
        refill(nowNanos)
        if (tokens >= 1.0) return 0
        val deficitNanos = (1.0 - tokens) / refillPerNano
        return ceil(deficitNanos / 1_000_000_000.0).toLong().coerceAtLeast(1)
    }

    private fun refill(nowNanos: Long) {
        if (nowNanos <= lastRefillNanos) return
        val elapsed = nowNanos - lastRefillNanos
        tokens = (tokens + elapsed * refillPerNano).coerceAtMost(capacity)
        lastRefillNanos = nowNanos
    }
}
