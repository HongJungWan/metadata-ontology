package com.hris.metadata.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 설정.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI metadataOntologyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Metadata Ontology API")
                        .description("도메인 용어와 물리 스키마를 잇는 경량 온톨로지 메타데이터 계층 (정산 도메인)")
                        .version("v1.1.0"));
    }
}
