package com.onebite.server.ratelimit

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class RateLimitFilterTest {

    private val mapper = ObjectMapper()

    /** 시계를 0 으로 고정 → 테스트 중 보충 없음(결정적). */
    private fun filter(authCapacity: Int = 2, defaultCapacity: Int = 5): RateLimitFilter {
        val fixed: () -> Long = { 0L }
        return RateLimitFilter(
            authLimiter = RateLimiter(authCapacity.toDouble(), authCapacity.toDouble(), 1000, fixed),
            defaultLimiter = RateLimiter(defaultCapacity.toDouble(), defaultCapacity.toDouble(), 1000, fixed),
            objectMapper = mapper,
        )
    }

    private fun request(uri: String, ip: String = "9.9.9.9", method: String = "POST") =
        MockHttpServletRequest(method, uri).apply { addHeader("X-Real-IP", ip) }

    @Test
    fun `인증 엔드포인트는 한도까지 허용 후 429`() {
        val f = filter(authCapacity = 2)

        repeat(2) {
            val chain = MockFilterChain()
            val res = MockHttpServletResponse()
            f.doFilter(request("/api/auth/kakao"), res, chain)
            assertNotNull(chain.request, "한도 내 요청은 통과해야 함")
            assertEquals(200, res.status)
        }

        val chain = MockFilterChain()
        val res = MockHttpServletResponse()
        f.doFilter(request("/api/auth/kakao"), res, chain)
        assertNull(chain.request, "한도 초과 요청은 다음 필터/컨트롤러로 가면 안 됨")
        assertEquals(429, res.status)
        assertNotNull(res.getHeader("Retry-After"))
        assertTrue(res.contentAsString.contains("Too Many Requests"))
    }

    @Test
    fun `IP 가 다르면 독립적으로 카운트된다`() {
        val f = filter(authCapacity = 1)

        val res1 = MockHttpServletResponse()
        f.doFilter(request("/api/auth/kakao", ip = "1.1.1.1"), res1, MockFilterChain())
        assertEquals(200, res1.status)

        // 같은 IP 두 번째 → 429
        val res1b = MockHttpServletResponse()
        f.doFilter(request("/api/auth/kakao", ip = "1.1.1.1"), res1b, MockFilterChain())
        assertEquals(429, res1b.status)

        // 다른 IP 는 영향 없음
        val res2 = MockHttpServletResponse()
        f.doFilter(request("/api/auth/kakao", ip = "2.2.2.2"), res2, MockFilterChain())
        assertEquals(200, res2.status)
    }

    @Test
    fun `auth 와 default 티어는 예산이 분리된다`() {
        val f = filter(authCapacity = 1, defaultCapacity = 1)

        // auth 소진
        f.doFilter(request("/api/auth/kakao"), MockHttpServletResponse(), MockFilterChain())
        val authDenied = MockHttpServletResponse()
        f.doFilter(request("/api/auth/kakao"), authDenied, MockFilterChain())
        assertEquals(429, authDenied.status)

        // default 는 별도 예산 → 같은 IP 라도 여전히 허용
        val def = MockHttpServletResponse()
        f.doFilter(request("/api/splits", method = "GET"), def, MockFilterChain())
        assertEquals(200, def.status)
    }

    @Test
    fun `제외 경로는 한도와 무관하게 통과한다`() {
        val f = filter(defaultCapacity = 1)
        repeat(5) {
            val chain = MockFilterChain()
            val res = MockHttpServletResponse()
            f.doFilter(request("/actuator/health", method = "GET"), res, chain)
            assertNotNull(chain.request)
            assertEquals(200, res.status)
        }
    }

    @Test
    fun `OPTIONS 프리플라이트는 카운트되지 않고 통과한다`() {
        val f = filter(authCapacity = 1)
        repeat(3) {
            val chain = MockFilterChain()
            val res = MockHttpServletResponse()
            f.doFilter(request("/api/auth/kakao", method = "OPTIONS"), res, chain)
            assertNotNull(chain.request)
        }
    }
}
