package com.onebite.server.ratelimit

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered

/**
 * 요청 속도 제한 구성.
 *
 *  - `nthing.ratelimit.enabled=false` 면 필터 자체를 등록하지 않음(로컬/테스트 비활성).
 *  - 토큰 버킷 capacity = 분당 허용치 = 보충 속도(분당) → 한 번에 분당 한도만큼 버스트 후 일정 속도 회복.
 *  - 필터는 `HIGHEST_PRECEDENCE` 로 Spring Security 보다 먼저 실행 → 비용 큰 인증 처리 전에 차단.
 */
@Configuration
@ConditionalOnProperty(
    prefix = "nthing.ratelimit",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
class RateLimitConfig(
    @Value("\${nthing.ratelimit.auth-requests-per-minute:20}") private val authPerMinute: Int,
    @Value("\${nthing.ratelimit.default-requests-per-minute:120}") private val defaultPerMinute: Int,
    @Value("\${nthing.ratelimit.max-tracked-clients:50000}") private val maxTrackedClients: Int,
) {
    @Bean
    fun rateLimitFilterRegistration(
        objectMapper: ObjectMapper,
    ): FilterRegistrationBean<RateLimitFilter> {
        val filter = RateLimitFilter(
            authLimiter = limiter(authPerMinute),
            defaultLimiter = limiter(defaultPerMinute),
            objectMapper = objectMapper,
        )
        return FilterRegistrationBean(filter).apply {
            order = Ordered.HIGHEST_PRECEDENCE
            addUrlPatterns("/*")
        }
    }

    private fun limiter(perMinute: Int): RateLimiter {
        val rate = perMinute.toDouble().coerceAtLeast(1.0)
        return RateLimiter(capacity = rate, refillPerMinute = rate, maxTrackedClients = maxTrackedClients)
    }
}
