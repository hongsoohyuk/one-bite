package com.onebite.server.config

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocsTest {

    @Autowired lateinit var mockMvc: MockMvc

    @Test
    fun `OpenAPI 문서가 생성되고 비인증으로 접근 가능하다`() {
        mockMvc.get("/v3/api-docs").andExpect {
            status { isOk() }
            jsonPath("$.info.title") { value("Nthing API") }
            jsonPath("$.components.securitySchemes.bearerAuth.scheme") { value("bearer") }
        }
    }
}
