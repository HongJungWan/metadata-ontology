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
import com.hris.metadata.domain.term.vo.TermId;
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
        // 코드값 표면형을 잔여 질의의 모든 토큰에서 찾는다 — 용어로 잡힌 토큰("홀드"→보류 용어)이라도 그 코드값
        // (settlement_status=HOLD)이 용어 매핑 컬럼과 무관하면 누락되던 문제를 해결(어휘 정렬, coverage↑).
        // BASELINE(expand off)은 항등 유지를 위해 빈 목록(코드 해석 미적용).
        List<String> codeSurfaceTokens = options.expandSynonyms()
                ? tokenize(normalized.residualQuery()) : List.of();
        List<ResolveResponse.ColumnMapping> columnMappings =
                buildColumnMappings(resolvedTerms.termIds(), codeSurfaceTokens);

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
        List<TermId> termIds = new ArrayList<>();
        List<String> unmapped = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (String token : expanded.expandedQuery().trim().split("\\s+")) {
            classifyToken(token, surfaceByCanonical, seen, terms, termIds, unmapped, useFuzzy);
        }
        return new ResolvedTerms(terms, termIds, unmapped);
    }

    private void classifyToken(String token, Map<String, String> surfaceByCanonical, Set<String> seen,
                               List<ResolveResponse.ResolvedTerm> terms, List<TermId> termIds,
                               List<String> unmapped, boolean useFuzzy) {
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
                    return;
                }
            }
        }
        unmapped.add(token);
    }

    private List<ResolveResponse.ColumnMapping> buildColumnMappings(
            List<TermId> termIds, List<String> codeSurfaceTokens) {
        List<ColumnMapping> rows = schemaMappingRepository.findColumnMappingsByTermIds(termIds);
        // 잔여 토큰에서 코드값 후보를 한 번에 찾는다(컬럼·코드 보존). 컬럼별 코드값으로도 인덱싱.
        List<CodeValueCandidate> hits = resolveCodeCandidates(codeSurfaceTokens);
        Map<String, String> codeByColumn = new LinkedHashMap<>();
        for (CodeValueCandidate hit : hits) {
            codeByColumn.putIfAbsent(columnKey(hit.physicalTable(), hit.physicalColumn()), hit.code());
        }

        List<ResolveResponse.ColumnMapping> mappings = new ArrayList<>();
        Set<String> emitted = new LinkedHashSet<>();
        // (1) 용어로 매핑된 컬럼의 코드값을 표면형(예: "미정산"→PENDING)으로 채운다 (PRD §4.1).
        for (ColumnMapping row : rows) {
            String code = row.codeValueRule();
            if (code == null) {
                code = codeByColumn.get(columnKey(row.physicalTable(), row.physicalColumn()));
            }
            mappings.add(new ResolveResponse.ColumnMapping(
                    row.physicalTable(), row.physicalColumn(), code));
            emitted.add(columnKey(row.physicalTable(), row.physicalColumn()));
        }

        // (2) 코드값 표면형(예: "홀드"→HOLD, "보류"/"대기")이 가리키는 컬럼이 용어 매핑에 없으면 직접 매핑으로 추가.
        // KS 등 /resolve 소비자가 코드 필터를 얻어 정형 질의 정밀도를 확보하게 한다(어휘 정렬, coverage↑).
        for (CodeValueCandidate hit : hits) {
            String key = columnKey(hit.physicalTable(), hit.physicalColumn());
            if (emitted.add(key)) {
                mappings.add(new ResolveResponse.ColumnMapping(
                        hit.physicalTable(), hit.physicalColumn(), hit.code()));
            }
        }
        return mappings;
    }

    /** 잔여 질의를 공백 토큰으로 나눈다(코드값 표면형 직매칭 대상). */
    private List<String> tokenize(String query) {
        List<String> tokens = new ArrayList<>();
        if (query == null || query.isBlank()) {
            return tokens;
        }
        for (String token : query.trim().split("\\s+")) {
            if (!token.isBlank()) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    /** 표면형 정확 일치하는 코드값 후보 목록(컬럼·코드 보존). */
    private List<CodeValueCandidate> resolveCodeCandidates(List<String> surfaces) {
        List<CodeValueCandidate> hits = new ArrayList<>();
        for (String surface : surfaces) {
            if (surface == null || surface.isBlank()) {
                continue;
            }
            for (CodeValueCandidate candidate : codeValueRepository.findCandidatesBySurface(surface)) {
                if (matchesSurface(candidate, surface)) {
                    hits.add(candidate);
                }
            }
        }
        return hits;
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
    private record ResolvedTerms(List<ResolveResponse.ResolvedTerm> terms, List<TermId> termIds,
                                 List<String> unmapped) {
    }
}
