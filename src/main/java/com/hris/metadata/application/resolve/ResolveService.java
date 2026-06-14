package com.hris.metadata.application.resolve;

import com.hris.metadata.domain.expand.ExpansionResult;
import com.hris.metadata.domain.expand.ExpansionService;
import com.hris.metadata.domain.normalize.NormalizationResult;
import com.hris.metadata.domain.normalize.NormalizationService;
import com.hris.metadata.domain.normalize.TimeRange;
import com.hris.metadata.domain.mapping.ColumnMapping;
import com.hris.metadata.domain.mapping.SchemaMappingRepository;
import com.hris.metadata.domain.schema.CodeValueCandidate;
import com.hris.metadata.domain.schema.CodeValueRepository;
import com.hris.metadata.domain.term.Term;
import com.hris.metadata.domain.term.TermRepository;
import com.hris.metadata.application.resolve.dto.response.ResolveResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * /resolve 오케스트레이션 서비스 (응용 서비스).
 * <p>
 * normalize(기간 추출) → expand(동의어→표준용어) → map(용어→물리 컬럼/코드값) 을 차례로 수행해
 * PRD §4.1 형식의 응답을 만든다. 매핑에 실패한 토큰은 unmapped 로 모은다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResolveService {

    private final NormalizationService normalizationService;
    private final ExpansionService expansionService;
    private final TermRepository termRepository;
    private final SchemaMappingRepository schemaMappingRepository;
    private final CodeValueRepository codeValueRepository;

    /**
     * 질의를 해석한다 (기준일 = 오늘).
     */
    public ResolveResponse resolve(String query) {
        return resolve(query, LocalDate.now());
    }

    /**
     * 질의를 해석한다 (기준일 명시 — 테스트용).
     */
    public ResolveResponse resolve(String query, LocalDate today) {
        return resolve(query, today, ResolveOptions.full());
    }

    /**
     * 질의를 해석한다 (단계 토글 명시 — 재현율 평가용).
     * <p>
     * 토글을 끄면 해당 단계를 항등 변환으로 대체한다: 정규화 off 는 기간 미추출(원문 유지),
     * 확장 off 는 원문 토큰 그대로 용어 조회. 평가의 baseline/full 이 같은 경로를 타게 한다.
     */
    public ResolveResponse resolve(String query, LocalDate today, ResolveOptions options) {
        NormalizationResult normalized = options.normalizeTime()
                ? normalizationService.normalize(query, today)
                : new NormalizationResult(null, query, null);
        ExpansionResult expanded = options.expandSynonyms()
                ? expansionService.expand(normalized.residualQuery())
                : new ExpansionResult(normalized.residualQuery(), List.of());

        // 퍼지는 정확 파이프라인(동의어 확장 + 표준용어 조회) 이후 진짜 미매핑 토큰에만 적용한다 —
        // 이미 유효한 표준용어가 유사 동의어로 치환돼 회귀하는 것을 막는다(오타/OOV 만 회복).
        ResolvedTerms resolvedTerms = resolveTerms(expanded, options.expandFuzzy());
        List<ResolveResponse.ColumnMapping> columnMappings =
                buildColumnMappings(resolvedTerms.termIds(), resolvedTerms.allExpansions());

        return ResolveResponse.builder()
                .normalizedQuery(buildNormalizedQuery(expanded.expandedQuery(), normalized.timeRange()))
                .terms(resolvedTerms.terms())
                .columnMappings(columnMappings)
                .timeRange(normalized.timeRange())
                .unmapped(resolvedTerms.unmapped())
                .build();
    }

    private ResolvedTerms resolveTerms(ExpansionResult expanded, boolean useFuzzy) {
        Map<String, String> surfaceByCanonical = new LinkedHashMap<>();
        for (ExpansionResult.TokenExpansion expansion : expanded.expansions()) {
            surfaceByCanonical.put(expansion.canonical(), expansion.surface());
        }

        List<ResolveResponse.ResolvedTerm> terms = new ArrayList<>();
        List<UUID> termIds = new ArrayList<>();
        List<String> unmapped = new ArrayList<>();
        List<ExpansionResult.TokenExpansion> fuzzyExpansions = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (String token : expanded.expandedQuery().trim().split("\\s+")) {
            classifyToken(token, surfaceByCanonical, seen, terms, termIds, unmapped, useFuzzy, fuzzyExpansions);
        }

        // 정확 확장 + 퍼지 회복 치환을 합쳐 코드값 해석(resolveCodeValues)에 함께 넘긴다.
        List<ExpansionResult.TokenExpansion> allExpansions = new ArrayList<>(expanded.expansions());
        allExpansions.addAll(fuzzyExpansions);
        return new ResolvedTerms(terms, termIds, unmapped, allExpansions);
    }

    private void classifyToken(String token, Map<String, String> surfaceByCanonical, Set<String> seen,
                               List<ResolveResponse.ResolvedTerm> terms, List<UUID> termIds,
                               List<String> unmapped, boolean useFuzzy,
                               List<ExpansionResult.TokenExpansion> fuzzyExpansions) {
        if (token.isBlank() || !seen.add(token)) {
            return;
        }
        Optional<Term> term = termRepository.findByCanonicalName(token);
        if (term.isPresent()) {
            terms.add(new ResolveResponse.ResolvedTerm(token, surfaceByCanonical.get(token)));
            termIds.add(term.get().getTermId());
            return;
        }
        // 정확 미스 → 퍼지 폴백(오타/OOV). 퍼지 표준용어가 실제 Term 으로 해석될 때만 채택.
        if (useFuzzy) {
            Optional<String> fuzzyCanonical = expansionService.fuzzyCanonical(token);
            if (fuzzyCanonical.isPresent()) {
                Optional<Term> fuzzyTerm = termRepository.findByCanonicalName(fuzzyCanonical.get());
                if (fuzzyTerm.isPresent()) {
                    terms.add(new ResolveResponse.ResolvedTerm(fuzzyCanonical.get(), token));
                    termIds.add(fuzzyTerm.get().getTermId());
                    fuzzyExpansions.add(new ExpansionResult.TokenExpansion(token, fuzzyCanonical.get()));
                    return;
                }
            }
        }
        unmapped.add(token);
    }

    private List<ResolveResponse.ColumnMapping> buildColumnMappings(
            List<UUID> termIds, List<ExpansionResult.TokenExpansion> expansions) {
        List<ColumnMapping> rows = schemaMappingRepository.findColumnMappingsByTermIds(termIds);
        // 표면형이 코드값(예: "미정산"→PENDING)을 가리키면 컬럼.코드값으로 미리 풀어둔다 (PRD §4.1).
        Map<String, String> codeByColumn = resolveCodeValues(expansions);

        List<ResolveResponse.ColumnMapping> mappings = new ArrayList<>();
        for (ColumnMapping row : rows) {
            String code = row.codeValueRule();
            if (code == null) {
                code = codeByColumn.get(columnKey(row.physicalTable(), row.physicalColumn()));
            }
            mappings.add(new ResolveResponse.ColumnMapping(
                    row.physicalTable(), row.physicalColumn(), code));
        }
        return mappings;
    }

    /**
     * 질의 표면형 중 코드값 사전(코드/라벨/동의어)에 정확히 일치하는 것을 찾아
     * "테이블.컬럼" → 코드 맵으로 돌려준다. 예: "미정산" → settlement.settlement_status → PENDING.
     * <p>
     * 해석된 용어와 무관한 순수 코드값 토큰(예: 용어 동의어가 아닌 "보류")은 여기서 다루지 않고
     * /match-sql-pattern 이 담당한다.
     */
    private Map<String, String> resolveCodeValues(List<ExpansionResult.TokenExpansion> expansions) {
        Map<String, String> codeByColumn = new LinkedHashMap<>();
        for (ExpansionResult.TokenExpansion expansion : expansions) {
            String surface = expansion.surface();
            for (CodeValueCandidate candidate : codeValueRepository.findCandidatesBySurface(surface)) {
                if (matchesSurface(candidate, surface)) {
                    codeByColumn.put(columnKey(
                            candidate.physicalTable(),
                            candidate.physicalColumn()), candidate.code());
                }
            }
        }
        return codeByColumn;
    }

    /** LIKE 1차 필터 결과를 코드/라벨/동의어 정확 일치로 재검증한다 (부분일치 오탐 방지). */
    private boolean matchesSurface(CodeValueCandidate candidate, String surface) {
        if (surface.equalsIgnoreCase(candidate.code()) || surface.equals(candidate.label())) {
            return true;
        }
        if (candidate.synonyms() == null) {
            return false;
        }
        for (String synonym : candidate.synonyms().split(",")) {
            if (synonym.trim().equals(surface)) {
                return true;
            }
        }
        return false;
    }

    private String columnKey(String table, String column) {
        return table + "." + column;
    }

    private String buildNormalizedQuery(String expandedQuery, TimeRange timeRange) {
        if (timeRange == null) {
            return expandedQuery.trim();
        }
        String period = timeRange.from() + "~" + timeRange.to();
        if (expandedQuery.isBlank()) {
            return period;
        }
        return expandedQuery.trim() + " " + period;
    }

    /** resolve 내부 집계 결과 */
    private record ResolvedTerms(List<ResolveResponse.ResolvedTerm> terms, List<UUID> termIds,
                                 List<String> unmapped,
                                 List<ExpansionResult.TokenExpansion> allExpansions) {
    }
}
