package com.hris.metadata.application.term.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * 용어-스키마 매핑 등록 커맨드 (DDD 2.3).
 * <p>JSON 필드명은 기존 SchemaMappingRequest 와 동일하다(REST 계약 불변).
 */
@Schema(description = "용어-스키마 매핑 요청")
public record MapTermToColumnCommand(

        @NotNull(message = "표준 용어 ID(termId)는 필수입니다.")
        @JsonProperty("termId")
        @Schema(description = "표준 용어 ID", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID termId,

        @NotNull(message = "스키마 카탈로그 ID(schemaCatalogId)는 필수입니다.")
        @JsonProperty("schemaCatalogId")
        @Schema(description = "물리 스키마 카탈로그 ID", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID schemaCatalogId,

        @JsonProperty("mappingType")
        @Schema(description = "매핑 유형", example = "DIRECT")
        String mappingType,

        @JsonProperty("codeValueRule")
        @Schema(description = "코드값 규칙", example = "PENDING")
        String codeValueRule
) {
    public MapTermToColumnCommand {
        if (termId == null) {
            throw new IllegalArgumentException("표준 용어 ID(termId)는 필수입니다.");
        }
        if (schemaCatalogId == null) {
            throw new IllegalArgumentException("스키마 카탈로그 ID(schemaCatalogId)는 필수입니다.");
        }
    }
}
