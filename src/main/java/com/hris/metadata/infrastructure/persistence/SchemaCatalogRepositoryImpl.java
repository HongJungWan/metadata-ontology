package com.hris.metadata.infrastructure.persistence;

import com.hris.metadata.domain.schema.SchemaCatalog;
import com.hris.metadata.domain.schema.SchemaCatalogRepository;
import com.hris.metadata.domain.schema.vo.SchemaCatalogId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SchemaCatalogRepository 포트의 어댑터 (infrastructure). Spring Data JPA 에 위임한다.
 */
@Repository
@RequiredArgsConstructor
public class SchemaCatalogRepositoryImpl implements SchemaCatalogRepository {

    private final SchemaCatalogJpaRepository jpa;

    @Override
    public SchemaCatalog save(SchemaCatalog schemaCatalog) {
        return jpa.save(schemaCatalog);
    }

    @Override
    public long count() {
        return jpa.count();
    }

    @Override
    public List<SchemaCatalog> findAll() {
        return jpa.findAll();
    }

    @Override
    public Optional<SchemaCatalog> findById(SchemaCatalogId schemaCatalogId) {
        return jpa.findById(schemaCatalogId);
    }

    @Override
    public Optional<SchemaCatalog> findByPhysicalTableAndPhysicalColumn(String physicalTable, String physicalColumn) {
        return jpa.findByPhysicalTableValueAndPhysicalColumnValue(physicalTable, physicalColumn);
    }
}
