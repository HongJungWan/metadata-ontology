package com.hris.metadata.domain.mapping.repository;

import com.hris.metadata.domain.mapping.entity.SchemaMapping;
import com.hris.metadata.domain.mapping.repository.custom.SchemaMappingRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * 용어-스키마 매핑 리포지토리.
 */
public interface SchemaMappingRepository extends JpaRepository<SchemaMapping, UUID>, SchemaMappingRepositoryCustom {

    List<SchemaMapping> findAllByTermId(UUID termId);

    boolean existsByTermIdAndSchemaCatalogId(UUID termId, UUID schemaCatalogId);
}
