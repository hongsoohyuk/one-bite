package com.onebite.server.ratelimit

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.filter.OncePerRequestFilter

/**
 * IP 기준 요청 속도 제한 필터(보안 하드닝).
 *
 *  - 인증 엔드포인트(`/api/auth` 하위)는 더 엄격(authLimiter) — code 교환 등 비용 큰 처리 + 무차별 시도 방지.
 *    그 외 경로는 defaultLimiter.
 *  - 한도 초과 시 `429 Too Many Requests` + `Retry-After`. 본문은 JSON.
 *  - CORS 프리플라이트(OPTIONS) 와 헬스/문서/소켓/H2 콘솔 경로는 제외.
 *  - 클라이언트 IP: nginx 가 설정하는 `X-Real-IP`(= 실제 접속 IP) 우선 → `X-Forwarded-For` 말단
 *    (`$proxy_add_x_forwarded_for` 가 실제 IP 를 맨 뒤에 덧붙임) → `remoteAddr` 폴백.
 *
 * 필터는 Spring Security 체인보다 먼저 실행되므로(설정의 HIGHEST_PRECEDENCE), 한도 초과 응답에는
 * 앱 웹뷰가 본문을 읽을 수 있도록 CORS 헤더를 직접 실어 준다(토큰 헤더 인증이라 credentials 불필요).
 */
class RateLimitFilter(
    private val authLimiter: RateLimiter,
    private val defaultLimiter: RateLimiter,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        if (request.method.equals("OPTIONS", ignoreCase = true)) return true // CORS preflight
        val path = request.requestURI
        return EXCLUDED_PREFIXES.any { path.startsWith(it) }
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val isAuth = request.requestURI.startsWith("/api/auth")
        val limiter = if (isAuth) authLimiter else defaultLimiter
        val tier = if (isAuth) "auth" else "default"

        val decision = limiter.check("$tier:${clientIp(request)}")
        if (decision.allowed) {
            filterChain.doFilter(request, response)
        } else {
            writeTooManyRequests(request, response, decision.retryAfterSeconds)
        }
    }

    private fun writeTooManyRequests(
        request: HttpServletRequest,
        response: HttpServletResponse,
        retryAfterSeconds: Long,
    ) {
        response.status = HttpStatus.TOO_MANY_REQUESTS.value()
        response.setHeader(HttpHeaders.RETRY_AFTER, retryAfterSeconds.toString())
        response.characterEncoding = Charsets.UTF_8.name()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        request.getHeader(HttpHeaders.ORIGIN)?.takeIf { it.isNotBlank() }?.let { origin ->
            response.setHeader("Access-Control-Allow-Origin", origin)
            response.setHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN)
        }
        val body = mapOf(
            "status" to HttpStatus.TOO_MANY_REQUESTS.value(),
            "error" to "Too Many Requests",
            "message" to "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.",
        )
        objectMapper.writeValue(response.writer, body)
    }

    private fun clientIp(request: HttpServletRequest): String {
        request.getHeader("X-Real-IP")?.takeIf { it.isNotBlank() }?.let { return it.trim() }
        request.getHeader("X-Forwarded-For")?.takeIf { it.isNotBlank() }
            ?.let { return it.split(",").last().trim() }
        return request.remoteAddr ?: "unknown"
    }

    companion object {
        private val EXCLUDED_PREFIXES = listOf(
            "/actuator", "/ws", "/h2-console", "/error",
            "/swagger-ui", "/v3/api-docs",
        )
    }
}
