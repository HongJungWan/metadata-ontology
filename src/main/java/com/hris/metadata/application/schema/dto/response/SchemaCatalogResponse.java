package com.hris.metadata.application.schema.dto.response;

import com.hris.metadata.domain.schema.SchemaCatalog;
import com.hris.metadata.shared.ddd.PublishedLanguage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 물리 스키마 카탈로그 응답 — knowledge-search 가 list_schema 로 소비하는 OHS 발행 언어.
 * 필드 변경 시 다운스트림(knowledge-search {@code MetadataSchemaClient}) 협의 필수.
 */
@PublishedLanguage
@Schema(description = "물리 스키마 카탈로그 응답")
@Getter
@Builder
public class SchemaCatalogResponse {

    @Schema(description = "스키마 카탈로그 ID")
    private UUID schemaCatalogId;

    @Schema(description = "물리 테이블명", example = "settlement")
    private String physicalTable;

    @Schema(description = "물리 컬럼명", example = "settlement_status")
    private String physicalColumn;

    @Schema(description = "데이터 타입", example = "varchar")
    private String dataType;

    @Schema(description = "컬럼 설명")
    private String description;

    @Schema(description = "출처 시스템", example = "redshift")
    private String sourceSystem;

    public static SchemaCatalogResponse from(SchemaCatalog catalog) {
        return SchemaCatalogResponse.builder()
                .schemaCatalogId(catalog.getSchemaCatalogId().value())
                .physicalTable(catalog.getPhysicalTable().value())
                .physicalColumn(catalog.getPhysicalColumn().value())
                .dataType(catalog.getDataType() == null ? null : catalog.getDataType().value())
                .description(catalog.getDescription() == null ? null : catalog.getDescription().value())
                .sourceSystem(catalog.getSourceSystem() == null ? null : catalog.getSourceSystem().value())
                .build();
    }
}
