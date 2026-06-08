package com.hris.metadata.domain.schema;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 물리 스키마 카탈로그 리포지토리 포트 (도메인 소유, 구현은 infrastructure — DIP).
 */
public interface SchemaCatalogRepository {

    SchemaCatalog save(SchemaCatalog schemaCatalog);

    long count();

    List<SchemaCatalog> findAll();

    Optional<SchemaCatalog> findById(UUID schemaCatalogId);

    Optional<SchemaCatalog> findByPhysicalTableAndPhysicalColumn(String physicalTable, String physicalColumn);
}
