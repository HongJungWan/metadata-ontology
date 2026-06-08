package com.hris.metadata.domain.pattern;

import com.hris.metadata.shared.ddd.DomainService;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * SQL 패턴 매칭 도메인 서비스.
 * <p>
 * 키워드 목록을 받아 트리거 키워드가 일치하는 패턴들을 컬럼·연산자·값 후보로 반환한다.
 * 매칭은 priority 오름차순으로 정렬한다.
 */
@DomainService
@RequiredArgsConstructor
public class SqlPatternService {

    private final SqlPatternRepository sqlPatternRepository;

    /**
     * 키워드 목록에 매칭되는 SQL 패턴 후보를 조회한다.
     *
     * @param keywords 키워드 목록
     * @return 매칭 후보 (priority 오름차순, 중복 제거)
     */
    public List<SqlPatternMatch> match(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return List.of();
        }

        Set<String> seen = new LinkedHashSet<>();
        List<SqlPatternMatch> matches = new ArrayList<>();

        for (String keyword : keywords) {
            addMatchesForKeyword(keyword, seen, matches);
        }

        matches.sort(Comparator.comparingInt(SqlPatternMatch::getPriority));
        return matches;
    }

    private void addMatchesForKeyword(String keyword, Set<String> seen, List<SqlPatternMatch> matches) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        for (SqlPattern pattern : sqlPatternRepository.findByTriggerKeyword(keyword.trim())) {
            if (!pattern.matchesKeyword(keyword.trim())) {
                continue;
            }
            String dedupeKey = keyword.trim() + "|" + pattern.getSqlPatternId();
            if (seen.add(dedupeKey)) {
                matches.add(toMatch(keyword.trim(), pattern));
            }
        }
    }

    private SqlPatternMatch toMatch(String keyword, SqlPattern pattern) {
        return new SqlPatternMatch(
                keyword,
                pattern.getColumnTarget(),
                pattern.getOperator(),
                pattern.getValueTemplate(),
                pattern.getPriority());
    }
}
