package com.hris.metadata.infrastructure.persistence;

import com.hris.metadata.domain.term.QSynonym;
import com.hris.metadata.domain.term.QTerm;
import com.hris.metadata.domain.term.Synonym;
import com.hris.metadata.domain.term.SynonymMatch;
import com.hris.metadata.domain.term.SynonymRepository;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * SynonymRepository 포트의 어댑터 (infrastructure) — H2/기본 프로파일(postgres 제외).
 * <p>
 * 기본 CRUD 는 Spring Data JPA 에 위임하고, 표면형→표준용어 정확 조회는 QueryDSL 로 직접 구현한다.
 * 퍼지 매칭은 H2 에 pg_trgm 이 없으므로 동일 의미(트라이그램 Jaccard)의 Java 폴백으로 구현해
 * 재현율 하네스가 결정론적으로 측정할 수 있게 한다. 운영(postgres)은 {@code PostgresSynonymRepositoryImpl}
 * 이 pg_trgm GIN 인덱스로 대체한다.
 */
@Repository
@Profile("!postgres")
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
                .select(synonym.surface.value, term.canonicalName.value)
                .from(synonym)
                .join(term).on(synonym.termId.eq(term.termId))
                .where(synonym.surface.value.eq(surface))
                .fetchFirst();

        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(new SynonymMatch(
                result.get(synonym.surface.value),
                result.get(term.canonicalName.value)));
    }

    @Override
    public Optional<SynonymMatch> findBySurfaceFuzzy(String surface, double threshold) {
        if (surface == null || surface.isBlank()) {
            return Optional.empty();
        }
        QSynonym synonym = QSynonym.synonym;
        QTerm term = QTerm.term;

        // 후보 전체(표면형+표준명)를 한 번에 읽어 Java 트라이그램 유사도로 최상위를 고른다.
        // 동의어 규모가 작아(수백 건) 전수 비교로 충분하며, pg_trgm 과 동일한 Jaccard 의미를 따른다.
        List<Tuple> candidates = queryFactory
                .select(synonym.surface.value, term.canonicalName.value)
                .from(synonym)
                .join(term).on(synonym.termId.eq(term.termId))
                .fetch();

        Set<String> queryGrams = trigrams(surface);
        String bestCanonical = null;
        double bestSim = -1;
        for (Tuple candidate : candidates) {
            double sim = jaccard(queryGrams, trigrams(candidate.get(synonym.surface.value)));
            if (sim >= threshold && sim > bestSim) {
                bestSim = sim;
                bestCanonical = candidate.get(term.canonicalName.value);
            }
        }
        return bestCanonical == null
                ? Optional.empty()
                : Optional.of(new SynonymMatch(surface, bestCanonical));
    }

    /** pg_trgm 방식 트라이그램 집합(앞 2칸·뒤 1칸 공백 패딩, 소문자). */
    private static Set<String> trigrams(String value) {
        Set<String> grams = new HashSet<>();
        if (value == null || value.isBlank()) {
            return grams;
        }
        String padded = "  " + value.toLowerCase() + " ";
        for (int i = 0; i + 3 <= padded.length(); i++) {
            grams.add(padded.substring(i, i + 3));
        }
        return grams;
    }

    /** 트라이그램 Jaccard 유사도(pg_trgm similarity 와 동일 의미). */
    private static double jaccard(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        return (double) intersection.size() / union.size();
    }
}
