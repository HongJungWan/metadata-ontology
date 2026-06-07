package com.hris.metadata.domain.mapping.repository.custom.impl;

import com.hris.metadata.domain.mapping.entity.QSchemaMapping;
import com.hris.metadata.domain.mapping.repository.custom.SchemaMappingRepositoryCustom;
import com.hris.metadata.domain.mapping.repository.custom.dto.ColumnMappingRow;
import com.hris.metadata.domain.mapping.repository.custom.dto.QColumnMappingRow;
import com.hris.metadata.domain.schema.entity.QSchemaCatalog;
import com.hris.metadata.domain.term.entity.QTerm;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 용어-스키마 매핑 QueryDSL 커스텀 구현.
 */
@Repository
@RequiredArgsConstructor
public class SchemaMappingRepositoryCustomImpl implements SchemaMappingRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ColumnMappingRow> findColumnMappingsByTermIds(List<UUID> termIds) {
        if (termIds == null || termIds.isEmpty()) {
            return Collections.emptyList();
        }

        QSchemaMapping mapping = QSchemaMapping.schemaMapping;
        QTerm term = QTerm.term;
        QSchemaCatalog catalog = QSchemaCatalog.schemaCatalog;

        return queryFactory
                .select(new QColumnMappingRow(
                        term.termId,
                        term.canonicalName,
                        catalog.physicalTable,
                        catalog.physicalColumn,
                        mapping.codeValueRule))
                .from(mapping)
                .join(term).on(mapping.termId.eq(term.termId))
                .join(catalog).on(mapping.schemaCatalogId.eq(catalog.schemaCatalogId))
                .where(mapping.termId.in(termIds))
                .fetch();
    }
}
