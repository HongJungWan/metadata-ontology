package com.hris.metadata.application.schema.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * 코드값 등록 커맨드 (DDD 2.3).
 * <p>JSON 필드명은 기존 CodeValueRequest 와 동일하다(REST 계약 불변).
 */
@Schema(description = "코드값 요청")
public record AddCodeValueCommand(

        @NotNull(message = "스키마 카탈로그 ID(schemaCatalogId)는 필수입니다.")
        @JsonProperty("schemaCatalogId")
        @Schema(description = "스키마 카탈로그 ID", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID schemaCatalogId,

        @NotBlank(message = "코드(code)는 필수입니다.")
        @JsonProperty("code")
        @Schema(description = "코드", example = "PENDING", requiredMode = Schema.RequiredMode.REQUIRED)
        String code,

        @JsonProperty("label")
        @Schema(description = "라벨", example = "미정산")
        String label,

        @JsonProperty("synonyms")
        @Schema(description = "코드값 동의어 (콤마 구분)", example = "미정산,대기")
        String synonyms
) {
    public AddCodeValueCommand {
        if (schemaCatalogId == null) {
            throw new IllegalArgumentException("스키마 카탈로그 ID(schemaCatalogId)는 필수입니다.");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("코드(code)는 필수입니다.");
        }
    }
}
