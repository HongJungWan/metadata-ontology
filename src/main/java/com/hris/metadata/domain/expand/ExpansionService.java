package com.hris.metadata.domain.expand;

import com.hris.metadata.domain.term.SynonymMatch;
import com.hris.metadata.domain.term.SynonymRepository;
import com.hris.metadata.shared.ddd.DomainService;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 동의어 확장 도메인 서비스.
 * <p>
 * 질의의 각 토큰을 동의어 사전에서 찾아 표준 용어로 치환한다 (예: "세틀"→"정산상태", "머천트"→"가맹점").
 * 사전에 없는 토큰은 그대로 둔다.
 */
@DomainService
@RequiredArgsConstructor
public class ExpansionService {

    private final SynonymRepository synonymRepository;
    /** 퍼지 폴백 트라이그램 유사도 임계값(호출자/DomainServiceConfig 가 주입 — 도메인에 설정 의존 금지). */
    private final double fuzzyThreshold;

    /**
     * 질의를 공백 단위 토큰으로 나눠 동의어를 표준 용어로 확장한다(정확 매칭만).
     *
     * @param query 원본 질의
     * @return 확장된 질의 + 치환 내역
     */
    public ExpansionResult expand(String query) {
        if (query == null || query.isBlank()) {
            return new ExpansionResult(query == null ? "" : query, List.of());
        }

        List<ExpansionResult.TokenExpansion> expansions = new ArrayList<>();
        List<String> expandedTokens = new ArrayList<>();

        for (String token : query.trim().split("\\s+")) {
            String canonical = expandToken(token, expansions);
            expandedTokens.add(canonical);
        }

        return new ExpansionResult(String.join(" ", expandedTokens), expansions);
    }

    private String expandToken(String token, List<ExpansionResult.TokenExpansion> expansions) {
        Optional<SynonymMatch> match = synonymRepository.findBySurfaceWithTerm(token);
        if (match.isEmpty()) {
            return token;
        }
        String canonical = match.get().canonicalName();
        expansions.add(new ExpansionResult.TokenExpansion(token, canonical));
        return canonical;
    }

    /**
     * 토큰의 <b>퍼지(근사)</b> 표준 용어 — 정확 동의어·표준용어 매칭이 모두 실패한 토큰의 폴백.
     * <p>
     * 호출자(ResolveService)가 정확 파이프라인 이후 <b>진짜 미매핑 토큰</b>에만 적용한다. 이렇게 하면
     * 이미 유효한 표준용어(예: "수수료율")가 유사 동의어로 잘못 치환돼 정확 매칭이 회귀하는 것을 막는다
     * (퍼지는 오직 오타·OOV 회복에만 기여). 임계값은 주입된 {@link #fuzzyThreshold}.
     *
     * @param token 정확 매칭이 실패한 토큰
     * @return 퍼지로 찾은 표준 용어 정식 명칭(없으면 empty)
     */
    public Optional<String> fuzzyCanonical(String token) {
        return synonymRepository.findBySurfaceFuzzy(token, fuzzyThreshold)
                .map(SynonymMatch::canonicalName);
    }
}
