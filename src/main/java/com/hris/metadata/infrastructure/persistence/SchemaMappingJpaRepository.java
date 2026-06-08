package com.hris.metadata.infrastructure.persistence;

import com.hris.metadata.domain.mapping.SchemaMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * SchemaMapping Spring Data JPA 어댑터 (infrastructure).
 */
public interface SchemaMappingJpaRepository extends JpaRepository<SchemaMapping, UUID> {

    List<SchemaMapping> findAllByTermId(UUID termId);

    boolean existsByTermIdAndSchemaCatalogId(UUID termId, UUID schemaCatalogId);
}
