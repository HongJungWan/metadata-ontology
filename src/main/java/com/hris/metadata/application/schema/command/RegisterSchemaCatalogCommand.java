package com.hris.metadata.application.schema.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 물리 스키마 카탈로그 등록/수정 커맨드 (DDD 2.3).
 * <p>JSON 필드명은 기존 SchemaCatalogRequest 와 동일하다(REST 계약 불변).
 */
@Schema(description = "물리 스키마 카탈로그 요청")
public record RegisterSchemaCatalogCommand(

        @NotBlank(message = "물리 테이블명(physicalTable)은 필수입니다.")
        @JsonProperty("physicalTable")
        @Schema(description = "물리 테이블명", example = "settlement", requiredMode = Schema.RequiredMode.REQUIRED)
        String physicalTable,

        @NotBlank(message = "물리 컬럼명(physicalColumn)은 필수입니다.")
        @JsonProperty("physicalColumn")
        @Schema(description = "물리 컬럼명", example = "settlement_status", requiredMode = Schema.RequiredMode.REQUIRED)
        String physicalColumn,

        @JsonProperty("dataType")
        @Schema(description = "데이터 타입", example = "varchar")
        String dataType,

        @JsonProperty("description")
        @Schema(description = "컬럼 설명", example = "정산 상태 코드")
        String description,

        @JsonProperty("sourceSystem")
        @Schema(description = "출처 시스템", example = "redshift")
        String sourceSystem
) {
    public RegisterSchemaCatalogCommand {
        if (physicalTable == null || physicalTable.isBlank()) {
            throw new IllegalArgumentException("물리 테이블명(physicalTable)은 필수입니다.");
        }
        if (physicalColumn == null || physicalColumn.isBlank()) {
            throw new IllegalArgumentException("물리 컬럼명(physicalColumn)은 필수입니다.");
        }
    }
}
