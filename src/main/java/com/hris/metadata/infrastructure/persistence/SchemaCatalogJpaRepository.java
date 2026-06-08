package com.hris.metadata.infrastructure.persistence;

import com.hris.metadata.domain.schema.SchemaCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * SchemaCatalog Spring Data JPA 어댑터 (infrastructure).
 */
public interface SchemaCatalogJpaRepository extends JpaRepository<SchemaCatalog, UUID> {

    Optional<SchemaCatalog> findByPhysicalTableAndPhysicalColumn(String physicalTable, String physicalColumn);
}
