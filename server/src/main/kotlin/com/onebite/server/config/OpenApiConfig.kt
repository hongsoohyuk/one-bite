package com.onebite.server.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * springdoc-openapi 메타데이터.
 *
 * 컨트롤러(@RestController) 매핑은 자동 스캔되므로 여기서는 문서 제목/버전과
 * 전역 JWT Bearer 인증 스킴(Swagger UI 의 Authorize 버튼)만 선언한다.
 *
 * api-docs/swagger-ui 노출은 `springdoc.*` 로 토글(운영 프로필에선 기본 비활성 — application-prod.properties).
 */
@Configuration
class OpenApiConfig {

    @Bean
    fun nthingOpenApi(): OpenAPI {
        val bearer = "bearerAuth"
        return OpenAPI()
            .info(
                Info()
                    .title("Nthing API")
                    .description("엔띵(Nthing) 서버 REST API. 보호된 엔드포인트는 `Authorization: Bearer <JWT>` 필요.")
                    .version("v1"),
            )
            .components(
                Components().addSecuritySchemes(
                    bearer,
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"),
                ),
            )
            .addSecurityItem(SecurityRequirement().addList(bearer))
    }
}
