package com.onebite.server.chat

import com.onebite.server.auth.JwtProvider
import com.onebite.server.split.CreateSplitDto
import com.onebite.server.split.SplitParticipantRepository
import com.onebite.server.split.SplitRepository
import com.onebite.server.split.SplitService
import com.onebite.server.user.AuthProvider
import com.onebite.server.user.User
import com.onebite.server.user.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
class ChatControllerIntegrationTest {
    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var jwtProvider: JwtProvider
    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var splitRepository: SplitRepository
    @Autowired lateinit var participantRepository: SplitParticipantRepository
    @Autowired lateinit var chatMessageRepository: ChatMessageRepository
    @Autowired lateinit var chatReadStateRepository: ChatReadStateRepository
    @Autowired lateinit var splitService: SplitService

    private lateinit var author: User
    private lateinit var joiner: User
    private lateinit var stranger: User
    private lateinit var tokenAuthor: String
    private lateinit var tokenJoiner: String
    private lateinit var tokenStranger: String

    @BeforeEach
    fun setup() {
        chatReadStateRepository.deleteAll()
        chatMessageRepository.deleteAll()
        participantRepository.deleteAll(); splitRepository.deleteAll(); userRepository.deleteAll()
        author = userRepository.save(User(provider = AuthProvider.KAKAO, providerId = "au${System.nanoTime()}", nickname = "작성자"))
        joiner = userRepository.save(User(provider = AuthProvider.KAKAO, providerId = "jo${System.nanoTime()}", nickname = "참여자"))
        stranger = userRepository.save(User(provider = AuthProvider.KAKAO, providerId = "st${System.nanoTime()}", nickname = "구경꾼"))
        tokenAuthor = jwtProvider.generateToken(author.id)
        tokenJoiner = jwtProvider.generateToken(joiner.id)
        tokenStranger = jwtProvider.generateToken(stranger.id)
    }

    // 다른 테스트 클래스에 FK 로 묶인 잔여 행(split_requests 등)을 남기지 않도록 정리
    @AfterEach
    fun tearDown() {
        chatReadStateRepository.deleteAll()
        chatMessageRepository.deleteAll()
        participantRepository.deleteAll(); splitRepository.deleteAll(); userRepository.deleteAll()
    }

    private fun matchedSplit(): Long {
        val s = splitService.create(
            CreateSplitDto(
                productName = "두쫀쿠", totalPrice = 20000, totalQty = 4, splitCount = 2,
                imageUrl = null, latitude = 37.5665, longitude = 126.9780, address = "서울",
            ),
            author.id,
        )
        splitService.join(s.id, joiner.id)
        return s.id
    }

    @Test
    fun `메시지 조회 비인증 401`() {
        val id = matchedSplit()
        mockMvc.get("/api/splits/$id/chat/messages").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `비멤버는 메시지 조회 403`() {
        val id = matchedSplit()
        mockMvc.get("/api/splits/$id/chat/messages") {
            header("Authorization", "Bearer $tokenStranger")
        }.andExpect { status { isForbidden() } }
    }

    @Test
    fun `멤버는 메시지 전송 후 조회 가능`() {
        val id = matchedSplit()
        mockMvc.post("/api/splits/$id/chat/messages") {
            header("Authorization", "Bearer $tokenAuthor")
            contentType = MediaType.APPLICATION_JSON
            content = """{"content":"몇 시에 만날까요?"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.content") { value("몇 시에 만날까요?") }
            jsonPath("$.senderId") { value(author.id) }
        }

        mockMvc.get("/api/splits/$id/chat/messages") {
            header("Authorization", "Bearer $tokenJoiner")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].content") { value("몇 시에 만날까요?") }
        }
    }

    @Test
    fun `빈 내용 전송 400`() {
        val id = matchedSplit()
        mockMvc.post("/api/splits/$id/chat/messages") {
            header("Authorization", "Bearer $tokenAuthor")
            contentType = MediaType.APPLICATION_JSON
            content = """{"content":"  "}"""
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `비멤버는 전송 403`() {
        val id = matchedSplit()
        mockMvc.post("/api/splits/$id/chat/messages") {
            header("Authorization", "Bearer $tokenStranger")
            contentType = MediaType.APPLICATION_JSON
            content = """{"content":"끼어들기"}"""
        }.andExpect { status { isForbidden() } }
    }

    @Test
    fun `안읽음 수는 내가 보낸 건 제외하고 읽음 처리 후 0`() {
        val id = matchedSplit()
        // 참여자가 2건 전송 → 작성자 입장 안읽음 2
        repeat(2) { i ->
            mockMvc.post("/api/splits/$id/chat/messages") {
                header("Authorization", "Bearer $tokenJoiner")
                contentType = MediaType.APPLICATION_JSON
                content = """{"content":"msg$i"}"""
            }.andExpect { status { isCreated() } }
        }
        mockMvc.get("/api/splits/$id/chat/unread") {
            header("Authorization", "Bearer $tokenAuthor")
        }.andExpect { status { isOk() }; jsonPath("$.count") { value(2) } }

        // 발신자 본인(참여자)은 자기 메시지라 안읽음 0
        mockMvc.get("/api/splits/$id/chat/unread") {
            header("Authorization", "Bearer $tokenJoiner")
        }.andExpect { status { isOk() }; jsonPath("$.count") { value(0) } }

        // 작성자가 읽음 처리 → 0
        mockMvc.post("/api/splits/$id/chat/read") {
            header("Authorization", "Bearer $tokenAuthor")
        }.andExpect { status { isNoContent() } }
        mockMvc.get("/api/splits/$id/chat/unread") {
            header("Authorization", "Bearer $tokenAuthor")
        }.andExpect { status { isOk() }; jsonPath("$.count") { value(0) } }
    }
}
