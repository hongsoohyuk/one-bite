package com.onebite.server.ratelimit

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 키(=tier:clientIp)별 토큰 버킷 레지스트리.
 *
 * 메모리 보호: 추적 키가 maxTrackedClients 를 넘으면 가득 찬(=현재 제한 안 걸린, idle) 버킷을
 * 청소해 회수한다. 청소는 동시에 1개만 수행(AtomicBoolean 가드).
 *
 * 시계(nowNanos)는 주입 가능(테스트 결정성), 기본은 System.nanoTime.
 */
class RateLimiter(
    private val capacity: Double,
    private val refillPerMinute: Double,
    private val maxTrackedClients: Int,
    private val clock: () -> Long = System::nanoTime,
) {
    private val buckets = ConcurrentHashMap<String, TokenBucket>()
    private val sweeping = AtomicBoolean(false)

    data class Decision(val allowed: Boolean, val retryAfterSeconds: Long)

    /** key 에 대해 토큰 1개 소비 시도 후 허용/차단 결정. */
    fun check(key: String): Decision {
        val now = clock()
        if (buckets.size > maxTrackedClients) sweepIdle(now)
        val bucket = buckets.computeIfAbsent(key) { TokenBucket(capacity, refillPerMinute, now) }
        return if (bucket.tryConsume(now)) {
            Decision(allowed = true, retryAfterSeconds = 0)
        } else {
            Decision(allowed = false, retryAfterSeconds = bucket.secondsUntilNextToken(now))
        }
    }

    /** 가득 찬(이용 안 하는) 버킷 제거로 메모리 회수. */
    private fun sweepIdle(now: Long) {
        if (!sweeping.compareAndSet(false, true)) return
        try {
            buckets.entries.removeIf { it.value.isFull(now) }
        } finally {
            sweeping.set(false)
        }
    }

    /** 현재 추적 중인 키 수 (모니터링/테스트용). */
    fun trackedCount(): Int = buckets.size
}
