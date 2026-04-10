package com.onebite.server.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.onebite.server.auth.JwtProvider
import com.onebite.server.split.SplitParticipantRepository
import com.onebite.server.split.SplitRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @Autowired lateinit var jwtProvider: JwtProvider
    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var splitRepository: SplitRepository
    @Autowired lateinit var splitParticipantRepository: SplitParticipantRepository

    private lateinit var user: User
    private lateinit var token: String

    @BeforeEach
    fun setup() {
        splitParticipantRepository.deleteAll()
        splitRepository.deleteAll()
        userRepository.deleteAll()
        user = userRepository.save(User(provider = AuthProvider.KAKAO, providerId = "u1", nickname = "테스트유저"))
        token = jwtProvider.generateToken(user.id)
    }

    @Test
    fun `GET users me 인증 없이 401`() {
        mockMvc.get("/api/users/me").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `GET users me 프로필 조회`() {
        mockMvc.get("/api/users/me") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.nickname") { value("테스트유저") }
        }
    }

    @Test
    fun `PATCH users me 닉네임 수정`() {
        mockMvc.patch("/api/users/me") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"nickname":"새닉네임"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.nickname") { value("새닉네임") }
        }
    }
}
