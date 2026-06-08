package com.hris.metadata.domain.mapping;

import java.util.List;
import java.util.UUID;

/**
 * 용어-스키마 매핑 리포지토리 포트 (도메인 소유, 구현은 infrastructure — DIP).
 */
public interface SchemaMappingRepository {

    SchemaMapping save(SchemaMapping schemaMapping);

    java.util.Optional<SchemaMapping> findById(UUID schemaMappingId);

    List<SchemaMapping> findAllByTermId(UUID termId);

    boolean existsByTermIdAndSchemaCatalogId(UUID termId, UUID schemaCatalogId);

    /**
     * 표준 용어 ID 목록에 대해 매핑 + 물리 스키마 카탈로그를 한 번에 조회한다.
     * resolve 응답의 columnMappings 를 구성하는 데 사용한다.
     */
    List<ColumnMapping> findColumnMappingsByTermIds(List<UUID> termIds);
}
