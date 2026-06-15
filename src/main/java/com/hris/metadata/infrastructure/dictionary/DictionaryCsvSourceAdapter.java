package com.hris.metadata.infrastructure.dictionary;

import com.hris.metadata.application.term.port.DictionaryCsvSource;
import com.hris.metadata.domain.term.SynonymType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 사전 CSV 소스 ACL 어댑터 (infrastructure).
 * <p>
 * CSV 형식: {@code canonicalName,domain,definition,synonym,synonymType}
 * (synonym/synonymType 은 선택). 헤더 행(첫 컬럼 "canonicalName")은 건너뛴다.
 * 외부 표현(줄바꿈/공백/컬럼수/대소문자)을 번역·정규화하고, SynonymType 미지정/오인식은 COLLOQUIAL 기본값으로
 * 흡수한다. 컬럼수 불일치 등 구조적 문제는 skip 진단으로 남긴다(도메인 불변식 위반은 팩토리/VO 가 처리).
 */
@Component
public class DictionaryCsvSourceAdapter implements DictionaryCsvSource {

    private static final int COLUMN_COUNT = 5;

    @Override
    public Parsed parse(String csv) {
        List<Row> rows = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        if (csv == null || csv.isBlank()) {
            return new Parsed(rows, skipped);
        }
        String[] lines = csv.replace("\r\n", "\n").split("\n");
        for (int index = 0; index < lines.length; index++) {
            String line = lines[index].trim();
            int rowNumber = index + 1;
            if (line.isEmpty() || isHeader(line)) {
                continue;
            }
            String[] c = splitRow(line);
            if (c.length != COLUMN_COUNT) {
                // 컬럼 수가 다르면 자동 보정하지 않고 건너뛴다 — 조용한 데이터 손상 방지.
                skipped.add("행 " + rowNumber + ": 컬럼 수 불일치");
                continue;
            }
            rows.add(new Row(rowNumber,
                    blankToNull(c[0]), blankToNull(c[1]), blankToNull(c[2]),
                    blankToNull(c[3]), parseType(c[4], rowNumber, skipped)));
        }
        return new Parsed(rows, skipped);
    }

    private boolean isHeader(String line) {
        return line.toLowerCase().startsWith("canonicalname");
    }

    private String[] splitRow(String line) {
        String[] columns = line.split(",", -1);
        for (int i = 0; i < columns.length; i++) {
            columns[i] = columns[i].trim();
        }
        return columns;
    }

    private static String blankToNull(String v) {
        return (v == null || v.isBlank()) ? null : v;
    }

    /** 동의어 유형 번역: 미지정→COLLOQUIAL, 오인식→COLLOQUIAL(진단 남김). */
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
}
