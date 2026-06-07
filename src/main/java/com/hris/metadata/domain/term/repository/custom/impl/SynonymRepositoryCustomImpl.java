package com.hris.metadata.domain.term.repository.custom.impl;

import com.hris.metadata.domain.term.entity.QSynonym;
import com.hris.metadata.domain.term.entity.QTerm;
import com.hris.metadata.domain.term.entity.Synonym;
import com.hris.metadata.domain.term.repository.custom.SynonymRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 동의어 QueryDSL 커스텀 구현.
 */
@Repository
@RequiredArgsConstructor
public class SynonymRepositoryCustomImpl implements SynonymRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Synonym> findBySurfaceWithTerm(String surface) {
        QSynonym synonym = QSynonym.synonym;
        QTerm term = QTerm.term;

        Synonym result = queryFactory
                .selectFrom(synonym)
                .join(synonym.term, term)
                .fetchJoin()
                .where(synonym.surface.eq(surface))
                .fetchFirst();

        return Optional.ofNullable(result);
    }
}
