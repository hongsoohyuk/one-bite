package com.onebite.server.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

// Next.js의 middleware처럼 모든 요청을 가로채서 JWT 검증
@Component
class JwtFilter(
    private val jwtProvider: JwtProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Authorization: Bearer <token> 에서 토큰 추출
        val token = request.getHeader("Authorization")
            ?.removePrefix("Bearer ")

        if (token != null && jwtProvider.isValid(token)) {
            val userId = jwtProvider.getUserId(token)
            val auth = UsernamePasswordAuthenticationToken(userId, null, emptyList())
            SecurityContextHolder.getContext().authentication = auth
        }

        filterChain.doFilter(request, response)
    }
}
