package com.hris.metadata.domain.mapping;

import com.hris.metadata.domain.mapping.vo.SchemaMappingId;
import com.hris.metadata.domain.schema.vo.SchemaCatalogId;
import com.hris.metadata.domain.term.vo.TermId;

import java.util.List;

/**
 * 용어-스키마 매핑 리포지토리 포트 (도메인 소유, 구현은 infrastructure — DIP).
 */
public interface SchemaMappingRepository {

    SchemaMapping save(SchemaMapping schemaMapping);

    java.util.Optional<SchemaMapping> findById(SchemaMappingId schemaMappingId);

    List<SchemaMapping> findAllByTermId(TermId termId);

    boolean existsByTermIdAndSchemaCatalogId(TermId termId, SchemaCatalogId schemaCatalogId);

    /**
     * 표준 용어 ID 목록에 대해 매핑 + 물리 스키마 카탈로그를 한 번에 조회한다.
     * resolve 응답의 columnMappings 를 구성하는 데 사용한다.
     */
    List<ColumnMapping> findColumnMappingsByTermIds(List<TermId> termIds);
}
