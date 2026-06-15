package com.hris.metadata.infrastructure.persistence;

import com.hris.metadata.domain.term.QSynonym;
import com.hris.metadata.domain.term.QTerm;
import com.hris.metadata.domain.term.Synonym;
import com.hris.metadata.domain.term.SynonymMatch;
import com.hris.metadata.domain.term.SynonymRepository;
import com.hris.metadata.domain.term.vo.SynonymId;
import com.hris.metadata.domain.term.vo.TermId;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SynonymRepository 포트의 PostgreSQL 어댑터 — postgres 프로파일 전용.
 * <p>
 * CRUD·정확 매칭은 H2 경로와 동일(JPA/QueryDSL)하고, 퍼지 매칭만 pg_trgm {@code similarity()}(GIN 인덱스)로
 * 대체한다. 오타·띄어쓰기·근접 OOV 를 설명 가능한 어휘 유사도로 회복한다 — 임베딩(의미 유사도)을 쓰지
 * 않는 비대칭 설계의 MO 측 구현(의미 패러프레이즈 리콜은 knowledge-search 본문 벡터 담당).
 */
@Repository
@Profile("postgres")
public class PostgresSynonymRepositoryImpl implements SynonymRepository {

    private final SynonymJpaRepository jpa;
    private final JPAQueryFactory queryFactory;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PostgresSynonymRepositoryImpl(SynonymJpaRepository jpa, JPAQueryFactory queryFactory,
                                         NamedParameterJdbcTemplate jdbcTemplate) {
        this.jpa = jpa;
        this.queryFactory = queryFactory;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Synonym save(Synonym synonym) {
        return jpa.save(synonym);
    }

    @Override
    public long count() {
        return jpa.count();
    }

    @Override
    public Optional<Synonym> findById(SynonymId synonymId) {
        return jpa.findById(synonymId);
    }

    @Override
    public List<Synonym> findAllByTermId(TermId termId) {
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
        return Optional.of(new SynonymMatch(result.get(synonym.surface.value), result.get(term.canonicalName.value)));
    }

    @Override
    public Optional<SynonymMatch> findBySurfaceFuzzy(String surface, double threshold) {
        if (surface == null || surface.isBlank()) {
            return Optional.empty();
        }
        // pg_trgm: GIN 인덱스(idx_synonym_surface_trgm) 가 similarity 검색을 가속. 소프트삭제 필터 포함.
        String sql = "SELECT t.canonical_name"
                + " FROM meta.synonym s JOIN meta.term t ON s.term_id = t.term_id"
                + " WHERE s.deleted_at IS NULL AND t.deleted_at IS NULL"
                + " AND similarity(s.surface, :surface) >= :threshold"
                + " ORDER BY similarity(s.surface, :surface) DESC LIMIT 1";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("surface", surface)
                .addValue("threshold", threshold);
        List<String> canonicals = jdbcTemplate.queryForList(sql, params, String.class);
        return canonicals.isEmpty()
                ? Optional.empty()
                : Optional.of(new SynonymMatch(surface, canonicals.get(0)));
    }
}
