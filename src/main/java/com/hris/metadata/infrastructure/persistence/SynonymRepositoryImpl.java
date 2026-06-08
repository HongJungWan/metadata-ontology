package com.hris.metadata.infrastructure.persistence;

import com.hris.metadata.domain.term.QSynonym;
import com.hris.metadata.domain.term.QTerm;
import com.hris.metadata.domain.term.Synonym;
import com.hris.metadata.domain.term.SynonymMatch;
import com.hris.metadata.domain.term.SynonymRepository;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SynonymRepository 포트의 어댑터 (infrastructure).
 * <p>
 * 기본 CRUD 는 Spring Data JPA 에 위임하고, 표면형→표준용어 조회는 QueryDSL 로 직접 구현한다.
 */
@Repository
@RequiredArgsConstructor
public class SynonymRepositoryImpl implements SynonymRepository {

    private final SynonymJpaRepository jpa;
    private final JPAQueryFactory queryFactory;

    @Override
    public Synonym save(Synonym synonym) {
        return jpa.save(synonym);
    }

    @Override
    public long count() {
        return jpa.count();
    }

    @Override
    public Optional<Synonym> findById(UUID synonymId) {
        return jpa.findById(synonymId);
    }

    @Override
    public List<Synonym> findAllByTermId(UUID termId) {
        return jpa.findAllByTermId(termId);
    }

    @Override
    public Optional<SynonymMatch> findBySurfaceWithTerm(String surface) {
        QSynonym synonym = QSynonym.synonym;
        QTerm term = QTerm.term;

        Tuple result = queryFactory
                .select(synonym.surface, term.canonicalName)
                .from(synonym)
                .join(term).on(synonym.termId.eq(term.termId))
                .where(synonym.surface.eq(surface))
                .fetchFirst();

        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(new SynonymMatch(
                result.get(synonym.surface),
                result.get(term.canonicalName)));
    }
}
