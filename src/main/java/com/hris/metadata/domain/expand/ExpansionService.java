package com.hris.metadata.domain.expand;

import com.hris.metadata.domain.term.entity.Synonym;
import com.hris.metadata.domain.term.repository.SynonymRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 동의어 확장 서비스.
 * <p>
 * 질의의 각 토큰을 동의어 사전에서 찾아 표준 용어로 치환한다 (예: "세틀"→"정산", "머천트"→"가맹점").
 * 사전에 없는 토큰은 그대로 둔다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpansionService {

    private final SynonymRepository synonymRepository;

    /**
     * 질의를 공백 단위 토큰으로 나눠 동의어를 표준 용어로 확장한다.
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
        Optional<Synonym> synonym = synonymRepository.findBySurfaceWithTerm(token);
        if (synonym.isEmpty()) {
            return token;
        }
        String canonical = synonym.get().getTerm().getCanonicalName();
        expansions.add(new ExpansionResult.TokenExpansion(token, canonical));
        return canonical;
    }
}
