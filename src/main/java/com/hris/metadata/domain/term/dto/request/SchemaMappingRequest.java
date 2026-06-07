package com.hris.metadata.domain.term.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * 용어-스키마 매핑 등록/수정 요청.
 */
@Schema(description = "용어-스키마 매핑 요청")
@Getter
@Setter
@NoArgsConstructor
public class SchemaMappingRequest {

    @NotNull(message = "표준 용어 ID(termId)는 필수입니다.")
    @JsonProperty("termId")
    @Schema(description = "표준 용어 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID termId;

    @NotNull(message = "스키마 카탈로그 ID(schemaCatalogId)는 필수입니다.")
    @JsonProperty("schemaCatalogId")
    @Schema(description = "물리 스키마 카탈로그 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID schemaCatalogId;

    @JsonProperty("mappingType")
    @Schema(description = "매핑 유형", example = "DIRECT")
    private String mappingType;

    @JsonProperty("codeValueRule")
    @Schema(description = "코드값 규칙", example = "PENDING")
    private String codeValueRule;
}
