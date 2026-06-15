package com.hris.metadata.infrastructure.persistence;

import com.hris.metadata.domain.mapping.ColumnMapping;
import com.hris.metadata.domain.mapping.QSchemaMapping;
import com.hris.metadata.domain.mapping.SchemaMapping;
import com.hris.metadata.domain.mapping.SchemaMappingRepository;
import com.hris.metadata.domain.mapping.vo.SchemaMappingId;
import com.hris.metadata.domain.schema.QSchemaCatalog;
import com.hris.metadata.domain.schema.vo.SchemaCatalogId;
import com.hris.metadata.domain.term.QTerm;
import com.hris.metadata.domain.term.vo.TermId;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * SchemaMappingRepository 포트의 어댑터 (infrastructure).
 * <p>
 * 기본 CRUD 는 Spring Data JPA 에 위임하고, resolve 의 컬럼 매핑 조인은 QueryDSL 로 직접 구현한다.
 * Term/SchemaCatalog 는 객체 참조 없이 termId/schemaCatalogId 로 조인한다.
 */
@Repository
@RequiredArgsConstructor
public class SchemaMappingRepositoryImpl implements SchemaMappingRepository {

    private final SchemaMappingJpaRepository jpa;
    private final JPAQueryFactory queryFactory;

    @Override
    public SchemaMapping save(SchemaMapping schemaMapping) {
        return jpa.save(schemaMapping);
    }

    @Override
    public Optional<SchemaMapping> findById(SchemaMappingId schemaMappingId) {
        return jpa.findById(schemaMappingId);
    }

    @Override
    public List<SchemaMapping> findAllByTermId(TermId termId) {
        return jpa.findAllByTermId(termId);
    }

    @Override
    public boolean existsByTermIdAndSchemaCatalogId(TermId termId, SchemaCatalogId schemaCatalogId) {
        return jpa.existsByTermIdAndSchemaCatalogId(termId, schemaCatalogId);
    }

    @Override
    public List<ColumnMapping> findColumnMappingsByTermIds(List<TermId> termIds) {
        if (termIds == null || termIds.isEmpty()) {
            return Collections.emptyList();
        }

        QSchemaMapping mapping = QSchemaMapping.schemaMapping;
        QTerm term = QTerm.term;
        QSchemaCatalog catalog = QSchemaCatalog.schemaCatalog;

        return queryFactory
                .select(Projections.constructor(ColumnMapping.class,
                        term.termId.value,
                        term.canonicalName.value,
                        catalog.physicalTable.value,
                        catalog.physicalColumn.value,
                        mapping.codeValueRule.value))
                .from(mapping)
                .join(term).on(mapping.termId.eq(term.termId))
                .join(catalog).on(mapping.schemaCatalogId.eq(catalog.schemaCatalogId))
                .where(mapping.termId.in(termIds))
                // PromptContextService 의 테이블 헤더 그룹핑이 행 연속성에 의존 — 정렬로 보장
                .orderBy(catalog.physicalTable.value.asc(), catalog.physicalColumn.value.asc())
                .fetch();
    }
}
