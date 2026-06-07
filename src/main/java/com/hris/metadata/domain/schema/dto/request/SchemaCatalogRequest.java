package com.hris.metadata.domain.schema.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 물리 스키마 카탈로그 등록/수정 요청.
 */
@Schema(description = "물리 스키마 카탈로그 요청")
@Getter
@Setter
@NoArgsConstructor
public class SchemaCatalogRequest {

    @NotBlank(message = "물리 테이블명(physicalTable)은 필수입니다.")
    @JsonProperty("physicalTable")
    @Schema(description = "물리 테이블명", example = "settlement", requiredMode = Schema.RequiredMode.REQUIRED)
    private String physicalTable;

    @NotBlank(message = "물리 컬럼명(physicalColumn)은 필수입니다.")
    @JsonProperty("physicalColumn")
    @Schema(description = "물리 컬럼명", example = "settlement_status", requiredMode = Schema.RequiredMode.REQUIRED)
    private String physicalColumn;

    @JsonProperty("dataType")
    @Schema(description = "데이터 타입", example = "varchar")
    private String dataType;

    @JsonProperty("description")
    @Schema(description = "컬럼 설명", example = "정산 상태 코드")
    private String description;

    @JsonProperty("sourceSystem")
    @Schema(description = "출처 시스템", example = "redshift")
    private String sourceSystem;
}
