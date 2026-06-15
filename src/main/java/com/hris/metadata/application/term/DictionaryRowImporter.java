package com.hris.metadata.application.term;

import com.hris.metadata.application.term.port.DictionaryCsvSource;
import com.hris.metadata.domain.term.Synonym;
import com.hris.metadata.domain.term.SynonymRepository;
import com.hris.metadata.domain.term.Term;
import com.hris.metadata.domain.term.TermRepository;
import com.hris.metadata.domain.term.TermStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 사전 임포트 행 단위 등록기 (응용).
 * <p>
 * 한 행(표준 용어 + 그 동의어)을 <b>단일 트랜잭션</b>으로 등록한다 — 대량 임포트를 한 트랜잭션에 묶지 않고
 * 행을 일관성 단위로 분리해 부분 성공(행 단위 skip)을 가능케 한다(단일 애그리거트 트랜잭션 원칙 준수).
 * 도메인 불변식(canonicalName/surface 비공백 등)은 팩토리/VO 가 강제하며, 위반 시 해당 행만 skip 진단으로 흡수한다.
 */
@Service
@RequiredArgsConstructor
public class DictionaryRowImporter {

    private final TermRepository termRepository;
    private final SynonymRepository synonymRepository;

    /** @param skipped 검증 실패 행 진단 누적(부수효과). @return 이 행에서 생성된 용어/동의어 수. */
    @Transactional
    public RowResult importRow(DictionaryCsvSource.Row row, List<String> skipped) {
        Optional<Term> existing = termRepository.findByCanonicalName(row.canonicalName());
        Term term;
        int createdTerms;
        if (existing.isPresent()) {
            term = existing.get();
            createdTerms = 0;
        } else {
            try {
                term = Term.create(UUID.randomUUID(), row.canonicalName(), row.domain(), row.definition());
                term.changeStatus(TermStatus.DRAFT);
                termRepository.save(term);
            } catch (IllegalArgumentException e) {
                skipped.add("행 " + row.rowNumber() + ": " + e.getMessage());
                return RowResult.none();
            }
            createdTerms = 1;
        }

        int createdSynonyms = 0;
        if (row.hasSynonym()) {
            try {
                synonymRepository.save(Synonym.create(
                        UUID.randomUUID(), term.getTermId().value(), row.synonymSurface(), row.synonymType()));
                createdSynonyms = 1;
            } catch (IllegalArgumentException e) {
                skipped.add("행 " + row.rowNumber() + ": " + e.getMessage());
            }
        }
        return new RowResult(createdTerms, createdSynonyms);
    }

    /** 행 처리 결과 카운트. */
    public record RowResult(int createdTerms, int createdSynonyms) {
        public static RowResult none() {
            return new RowResult(0, 0);
        }
    }
}
