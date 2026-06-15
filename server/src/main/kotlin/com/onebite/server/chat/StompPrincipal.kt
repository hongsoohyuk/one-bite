package com.onebite.server.chat

import java.security.Principal

/** STOMP 세션에 실은 인증 주체 (userId) */
class StompPrincipal(val userId: Long) : Principal {
    override fun getName(): String = userId.toString()
}
