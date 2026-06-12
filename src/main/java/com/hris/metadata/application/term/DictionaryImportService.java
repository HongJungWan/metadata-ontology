package com.hris.metadata.application.term;

import com.hris.metadata.domain.term.Synonym;
import com.hris.metadata.domain.term.SynonymRepository;
import com.hris.metadata.domain.term.SynonymType;
import com.hris.metadata.domain.term.Term;
import com.hris.metadata.domain.term.TermRepository;
import com.hris.metadata.domain.term.TermStatus;
import com.hris.metadata.global.exception.BusinessException;
import com.hris.metadata.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 사전 CSV 일괄 임포트 서비스 (응용 서비스, PRD §5).
 * <p>
 * 팀별 용어·동의어를 시트/CSV 로 한 번에 올린다.
 * CSV 형식: {@code canonicalName,domain,definition,synonym,synonymType}
 * (synonym/synonymType 은 선택 — 비어 있으면 용어만 등록).
 * 헤더 행은 선택이며, 첫 컬럼이 "canonicalName" 이면 건너뛴다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DictionaryImportService {

    private static final int COLUMN_COUNT = 5;

    private final TermRepository termRepository;
    private final SynonymRepository synonymRepository;

    /**
     * CSV 본문을 파싱해 용어/동의어를 등록한다.
     *
     * @param csv CSV 전체 텍스트
     * @return 생성/건너뛴 건수 요약
     */
    public ImportResult importCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            throw new BusinessException(ErrorCode.IMPORT_PARSE_FAILED, "CSV 내용이 비어 있습니다.");
        }

        int createdTerms = 0;
        int createdSynonyms = 0;
        List<String> skipped = new ArrayList<>();

        String[] lines = csv.replace("\r\n", "\n").split("\n");
        for (int index = 0; index < lines.length; index++) {
            String line = lines[index].trim();
            if (line.isEmpty() || isHeader(line)) {
                continue;
            }
            RowResult result = importRow(line, index + 1, skipped);
            createdTerms += result.createdTerms();
            createdSynonyms += result.createdSynonyms();
        }

        return ImportResult.builder()
                .createdTerms(createdTerms)
                .createdSynonyms(createdSynonyms)
                .skipped(skipped)
                .build();
    }

    private boolean isHeader(String line) {
        return line.toLowerCase().startsWith("canonicalname");
    }

    private RowResult importRow(String line, int rowNumber, List<String> skipped) {
        String[] columns = splitRow(line);
        if (columns.length != COLUMN_COUNT) {
            // 컬럼 수가 다르면 자동 보정하지 않고 건너뛴다 — 조용한 데이터 손상 방지.
            skipped.add("행 " + rowNumber + ": 컬럼 수 불일치");
            return RowResult.none();
        }
        if (columns[0].isBlank()) {
            skipped.add("행 " + rowNumber + ": canonicalName 누락");
            return RowResult.none();
        }

        Optional<Term> existing = termRepository.findByCanonicalName(columns[0]);
        Term term;
        int createdTerms;
        if (existing.isPresent()) {
            term = existing.get();
            createdTerms = 0;
        } else {
            try {
                term = createTerm(columns);
            } catch (IllegalArgumentException e) {
                skipped.add("행 " + rowNumber + ": " + e.getMessage());
                return RowResult.none();
            }
            createdTerms = 1;
        }
        int createdSynonyms = createSynonymIfPresent(term.getTermId(), columns, rowNumber, skipped);
        return new RowResult(createdTerms, createdSynonyms);
    }

    private String[] splitRow(String line) {
        String[] columns = line.split(",", -1);
        for (int i = 0; i < columns.length; i++) {
            columns[i] = columns[i].trim();
        }
        return columns;
    }

    private Term createTerm(String[] columns) {
        Term term = Term.create(UUID.randomUUID(), columns[0],
                columns[1].isBlank() ? null : columns[1],
                columns[2].isBlank() ? null : columns[2]);
        term.changeStatus(TermStatus.DRAFT);
        termRepository.save(term);
        return term;
    }

    private int createSynonymIfPresent(UUID termId, String[] columns, int rowNumber, List<String> skipped) {
        if (columns[3].isBlank()) {
            return 0;
        }
        SynonymType type = parseType(columns[4], rowNumber, skipped);
        Synonym synonym = Synonym.create(UUID.randomUUID(), termId, columns[3], type);
        synonymRepository.save(synonym);
        return 1;
    }

    private SynonymType parseType(String raw, int rowNumber, List<String> skipped) {
        if (raw == null || raw.isBlank()) {
            return SynonymType.COLLOQUIAL;
        }
        try {
            return SynonymType.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            skipped.add("행 " + rowNumber + ": 알 수 없는 synonymType '" + raw + "' → COLLOQUIAL 적용");
            return SynonymType.COLLOQUIAL;
        }
    }

    /** 행 처리 결과 카운트 */
    private record RowResult(int createdTerms, int createdSynonyms) {
        static RowResult none() {
            return new RowResult(0, 0);
        }
    }
}
