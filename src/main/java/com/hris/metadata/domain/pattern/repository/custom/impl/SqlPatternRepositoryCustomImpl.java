package com.hris.metadata.domain.pattern.repository.custom.impl;

import com.hris.metadata.domain.pattern.entity.QSqlPattern;
import com.hris.metadata.domain.pattern.entity.SqlPattern;
import com.hris.metadata.domain.pattern.repository.custom.SqlPatternRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SQL 패턴 QueryDSL 커스텀 구현.
 */
@Repository
@RequiredArgsConstructor
public class SqlPatternRepositoryCustomImpl implements SqlPatternRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<SqlPattern> findByTriggerKeyword(String keyword) {
        QSqlPattern pattern = QSqlPattern.sqlPattern;

        return queryFactory
                .selectFrom(pattern)
                .where(pattern.triggerKeywords.contains(keyword))
                .orderBy(pattern.priority.asc())
                .fetch();
    }
}
