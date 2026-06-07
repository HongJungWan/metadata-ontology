package com.hris.metadata.domain.schema.repository;

import com.hris.metadata.domain.schema.entity.SchemaCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * 물리 스키마 카탈로그 리포지토리.
 */
public interface SchemaCatalogRepository extends JpaRepository<SchemaCatalog, UUID> {

    Optional<SchemaCatalog> findByPhysicalTableAndPhysicalColumn(String physicalTable, String physicalColumn);
}
