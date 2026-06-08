package com.hris.metadata.domain.pattern;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL 패턴 리포지토리 포트 (도메인 소유, 구현은 infrastructure — DIP).
 */
public interface SqlPatternRepository {

    SqlPattern save(SqlPattern sqlPattern);

    long count();

    List<SqlPattern> findAll();

    Optional<SqlPattern> findById(UUID sqlPatternId);

    /**
     * 키워드가 trigger_keywords 에 포함된 패턴을 priority 오름차순으로 조회한다.
     * LIKE 1차 필터 후 정확 일치 판정은 엔티티 도메인 메서드에 위임한다.
     */
    List<SqlPattern> findByTriggerKeyword(String keyword);
}
