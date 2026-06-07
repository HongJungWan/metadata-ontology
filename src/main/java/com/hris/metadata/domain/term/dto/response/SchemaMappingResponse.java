package com.hris.metadata.domain.term.dto.response;

import com.hris.metadata.domain.mapping.entity.SchemaMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 용어-스키마 매핑 응답.
 */
@Schema(description = "용어-스키마 매핑 응답")
@Getter
@Builder
public class SchemaMappingResponse {

    @Schema(description = "매핑 ID")
    private UUID schemaMappingId;

    @Schema(description = "표준 용어 ID")
    private UUID termId;

    @Schema(description = "물리 스키마 카탈로그 ID")
    private UUID schemaCatalogId;

    @Schema(description = "매핑 유형", example = "DIRECT")
    private String mappingType;

    @Schema(description = "코드값 규칙", example = "PENDING")
    private String codeValueRule;

    public static SchemaMappingResponse from(SchemaMapping mapping) {
        return SchemaMappingResponse.builder()
                .schemaMappingId(mapping.getSchemaMappingId())
                .termId(mapping.getTermId())
                .schemaCatalogId(mapping.getSchemaCatalogId())
                .mappingType(mapping.getMappingType())
                .codeValueRule(mapping.getCodeValueRule())
                .build();
    }
}
