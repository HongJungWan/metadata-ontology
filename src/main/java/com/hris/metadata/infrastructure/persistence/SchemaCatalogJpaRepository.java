package com.hris.metadata.infrastructure.persistence;

import com.hris.metadata.domain.schema.SchemaCatalog;
import com.hris.metadata.domain.schema.vo.SchemaCatalogId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * SchemaCatalog Spring Data JPA 어댑터 (infrastructure).
 */
public interface SchemaCatalogJpaRepository extends JpaRepository<SchemaCatalog, SchemaCatalogId> {

    Optional<SchemaCatalog> findByPhysicalTableValueAndPhysicalColumnValue(String physicalTable, String physicalColumn);
}
