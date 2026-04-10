package com.onebite.server.split

import com.fasterxml.jackson.databind.ObjectMapper
import com.onebite.server.auth.JwtProvider
import com.onebite.server.user.AuthProvider
import com.onebite.server.user.User
import com.onebite.server.user.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
class SplitControllerIntegrationTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @Autowired lateinit var jwtProvider: JwtProvider
    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var splitRepository: SplitRepository
    @Autowired lateinit var splitParticipantRepository: SplitParticipantRepository

    private lateinit var userA: User
    private lateinit var userB: User
    private lateinit var tokenA: String
    private lateinit var tokenB: String

    @BeforeEach
    fun setup() {
        splitParticipantRepository.deleteAll()
        splitRepository.deleteAll()
        userRepository.deleteAll()

        userA = userRepository.save(User(provider = AuthProvider.KAKAO, providerId = "a1", nickname = "유저A"))
        userB = userRepository.save(User(provider = AuthProvider.KAKAO, providerId = "b1", nickname = "유저B"))
        tokenA = jwtProvider.generateToken(userA.id)
        tokenB = jwtProvider.generateToken(userB.id)
    }

    private fun createSplitDto() = CreateSplitDto(
        productName = "두쫀쿠 4개입",
        totalPrice = 20000,
        totalQty = 4,
        splitCount = 2,
        latitude = 37.5665,
        longitude = 126.9780,
        address = "서울시 중구"
    )

    @Test
    fun `GET splits 비인증 조회 가능`() {
        mockMvc.get("/api/splits").andExpect { status { isOk() } }
    }

    @Test
    fun `POST splits 인증 필요`() {
        mockMvc.post("/api/splits") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(createSplitDto())
        }.andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `POST splits 인증 후 생성 성공`() {
        mockMvc.post("/api/splits") {
            header("Authorization", "Bearer $tokenA")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(createSplitDto())
        }.andExpect {
            status { isCreated() }
            jsonPath("$.productName") { value("두쫀쿠 4개입") }
            jsonPath("$.author.nickname") { value("유저A") }
            jsonPath("$.status") { value("WAITING") }
        }
    }

    @Test
    fun `GET splits by id 조회`() {
        val result = mockMvc.post("/api/splits") {
            header("Authorization", "Bearer $tokenA")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(createSplitDto())
        }.andReturn()

        val id = objectMapper.readTree(result.response.contentAsString)["id"].asLong()

        mockMvc.get("/api/splits/$id").andExpect {
            status { isOk() }
            jsonPath("$.id") { value(id) }
        }
    }

    @Test
    fun `GET splits 존재하지 않는 id 404`() {
        mockMvc.get("/api/splits/99999").andExpect { status { isNotFound() } }
    }

    @Test
    fun `POST join 참여 성공`() {
        val result = mockMvc.post("/api/splits") {
            header("Authorization", "Bearer $tokenA")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(createSplitDto())
        }.andReturn()

        val id = objectMapper.readTree(result.response.contentAsString)["id"].asLong()

        mockMvc.post("/api/splits/$id/join") {
            header("Authorization", "Bearer $tokenB")
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("MATCHED") }
            jsonPath("$.currentParticipants") { value(2) }
        }
    }

    @Test
    fun `POST join 본인 글 참여 불가`() {
        val result = mockMvc.post("/api/splits") {
            header("Authorization", "Bearer $tokenA")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(createSplitDto())
        }.andReturn()

        val id = objectMapper.readTree(result.response.contentAsString)["id"].asLong()

        mockMvc.post("/api/splits/$id/join") {
            header("Authorization", "Bearer $tokenA")
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `PATCH cancel 작성자만 취소 가능`() {
        val result = mockMvc.post("/api/splits") {
            header("Authorization", "Bearer $tokenA")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(createSplitDto())
        }.andReturn()

        val id = objectMapper.readTree(result.response.contentAsString)["id"].asLong()

        // 다른 유저가 취소 시도 → 403
        mockMvc.patch("/api/splits/$id/cancel") {
            header("Authorization", "Bearer $tokenB")
        }.andExpect { status { isForbidden() } }

        // 작성자가 취소 → 성공
        mockMvc.patch("/api/splits/$id/cancel") {
            header("Authorization", "Bearer $tokenA")
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("CANCELLED") }
        }
    }

    @Test
    fun `GET splits my 인증 필요`() {
        mockMvc.get("/api/splits/my").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `POST splits validation 에러`() {
        mockMvc.post("/api/splits") {
            header("Authorization", "Bearer $tokenA")
            contentType = MediaType.APPLICATION_JSON
            content = """{"productName":"","totalPrice":0,"totalQty":0,"splitCount":1,"latitude":0,"longitude":0,"address":"addr"}"""
        }.andExpect { status { isBadRequest() } }
    }
}
