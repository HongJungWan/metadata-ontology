package com.hris.metadata.domain.pattern.repository.custom;

import com.hris.metadata.domain.pattern.entity.SqlPattern;

import java.util.List;

/**
 * SQL 패턴 QueryDSL 커스텀 리포지토리.
 */
public interface SqlPatternRepositoryCustom {

    /**
     * 키워드가 trigger_keywords 에 포함된 패턴을 priority 오름차순으로 조회한다.
     * LIKE 1차 필터 후 정확 일치 판정은 엔티티 도메인 메서드에 위임한다.
     */
    List<SqlPattern> findByTriggerKeyword(String keyword);
}
