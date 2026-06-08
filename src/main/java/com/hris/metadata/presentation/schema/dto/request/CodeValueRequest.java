package com.hris.metadata.presentation.schema.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * 코드값 등록/수정 요청.
 */
@Schema(description = "코드값 요청")
@Getter
@Setter
@NoArgsConstructor
public class CodeValueRequest {

    @NotNull(message = "스키마 카탈로그 ID(schemaCatalogId)는 필수입니다.")
    @JsonProperty("schemaCatalogId")
    @Schema(description = "스키마 카탈로그 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID schemaCatalogId;

    @NotBlank(message = "코드(code)는 필수입니다.")
    @JsonProperty("code")
    @Schema(description = "코드", example = "PENDING", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @JsonProperty("label")
    @Schema(description = "라벨", example = "미정산")
    private String label;

    @JsonProperty("synonyms")
    @Schema(description = "코드값 동의어 (콤마 구분)", example = "미정산,대기")
    private String synonyms;
}
