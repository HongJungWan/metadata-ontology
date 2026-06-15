package com.hris.metadata.application.term;

import com.hris.metadata.application.term.port.DictionaryCsvSource;
import com.hris.metadata.global.exception.BusinessException;
import com.hris.metadata.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 사전 CSV 일괄 임포트 서비스 (응용 서비스 — 흐름 제어만, PRD §5).
 * <p>
 * CSV 파싱·검증·기본값은 ACL({@link DictionaryCsvSource})이, 행 등록·트랜잭션은
 * {@link DictionaryRowImporter}(행 단위 트랜잭션)가 담당한다. 본 서비스는 파싱→행별 등록→집계의
 * 오케스트레이션만 한다(비즈니스 로직/검증 미보유).
 */
@Service
@RequiredArgsConstructor
public class DictionaryImportService {

    private final DictionaryCsvSource csvSource;
    private final DictionaryRowImporter rowImporter;

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

        DictionaryCsvSource.Parsed parsed = csvSource.parse(csv);
        List<String> skipped = new ArrayList<>(parsed.skipped());
        int createdTerms = 0;
        int createdSynonyms = 0;

        for (DictionaryCsvSource.Row row : parsed.rows()) {
            DictionaryRowImporter.RowResult result = rowImporter.importRow(row, skipped);
            createdTerms += result.createdTerms();
            createdSynonyms += result.createdSynonyms();
        }

        return ImportResult.builder()
                .createdTerms(createdTerms)
                .createdSynonyms(createdSynonyms)
                .skipped(skipped)
                .build();
    }
}
