package com.hris.metadata.infrastructure.persistence;

import com.hris.metadata.domain.pattern.QSqlPattern;
import com.hris.metadata.domain.pattern.SqlPattern;
import com.hris.metadata.domain.pattern.SqlPatternRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SqlPatternRepository 포트의 어댑터 (infrastructure).
 * <p>
 * 기본 CRUD 는 Spring Data JPA 에 위임하고, 트리거 키워드 조회는 QueryDSL 로 직접 구현한다.
 */
@Repository
@RequiredArgsConstructor
public class SqlPatternRepositoryImpl implements SqlPatternRepository {

    private final SqlPatternJpaRepository jpa;
    private final JPAQueryFactory queryFactory;

    @Override
    public SqlPattern save(SqlPattern sqlPattern) {
        return jpa.save(sqlPattern);
    }

    @Override
    public long count() {
        return jpa.count();
    }

    @Override
    public List<SqlPattern> findAll() {
        return jpa.findAll();
    }

    @Override
    public Optional<SqlPattern> findById(UUID sqlPatternId) {
        return jpa.findById(sqlPatternId);
    }

    @Override
    public List<SqlPattern> findByTriggerKeyword(String keyword) {
        QSqlPattern pattern = QSqlPattern.sqlPattern;

        return queryFactory
                .selectFrom(pattern)
                .where(pattern.triggerKeywords.value.contains(keyword))
                .orderBy(pattern.priority.asc())
                .fetch();
    }
}
