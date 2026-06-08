package com.hris.metadata.presentation.schema.dto.response;

import com.hris.metadata.domain.schema.CodeValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 코드값 응답.
 */
@Schema(description = "코드값 응답")
@Getter
@Builder
public class CodeValueResponse {

    @Schema(description = "코드값 ID")
    private UUID codeValueId;

    @Schema(description = "스키마 카탈로그 ID")
    private UUID schemaCatalogId;

    @Schema(description = "코드", example = "PENDING")
    private String code;

    @Schema(description = "라벨", example = "미정산")
    private String label;

    @Schema(description = "코드값 동의어 (콤마 구분)", example = "미정산,대기")
    private String synonyms;

    public static CodeValueResponse from(CodeValue codeValue) {
        return CodeValueResponse.builder()
                .codeValueId(codeValue.getCodeValueId())
                .schemaCatalogId(codeValue.getSchemaCatalogId())
                .code(codeValue.getCode())
                .label(codeValue.getLabel())
                .synonyms(codeValue.getSynonyms())
                .build();
    }
}
