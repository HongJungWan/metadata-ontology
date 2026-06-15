package com.hris.metadata.infrastructure.persistence;

import com.hris.metadata.domain.mapping.SchemaMapping;
import com.hris.metadata.domain.mapping.vo.SchemaMappingId;
import com.hris.metadata.domain.schema.vo.SchemaCatalogId;
import com.hris.metadata.domain.term.vo.TermId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * SchemaMapping Spring Data JPA 어댑터 (infrastructure).
 */
public interface SchemaMappingJpaRepository extends JpaRepository<SchemaMapping, SchemaMappingId> {

    List<SchemaMapping> findAllByTermId(TermId termId);

    boolean existsByTermIdAndSchemaCatalogId(TermId termId, SchemaCatalogId schemaCatalogId);
}
