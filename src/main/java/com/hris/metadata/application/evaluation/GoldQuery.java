package com.hris.metadata.application.evaluation;

import java.util.List;

/**
 * 재현율 평가 정답셋 한 건 (응용 결과 홀더).
 * <p>
 * {@code evaluation/gold_queries.csv} 의 한 행 — 질의와 그 질의가 반드시 찾아야 하는
 * (테이블, 컬럼[, 코드값]) 기대 매핑 목록.
 */
public record GoldQuery(
        String queryId,
        String query,
        List<ExpectedMapping> expected,
        boolean expectsTimeRange) {

    /**
     * 기대 매핑 항목. 표기 형식: {@code table.column} 또는 {@code table.column=CODE}.
     */
    public record ExpectedMapping(String physicalTable, String physicalColumn, String code) {

        /** "table.column[=CODE]" 표기를 파싱한다. */
        public static ExpectedMapping parse(String notation) {
            String columnPart = notation;
            String code = null;
            int eq = notation.indexOf('=');
            if (eq >= 0) {
                columnPart = notation.substring(0, eq);
                code = notation.substring(eq + 1);
            }
            int dot = columnPart.indexOf('.');
            if (dot <= 0 || dot == columnPart.length() - 1) {
                throw new IllegalArgumentException("기대 매핑 표기는 table.column[=CODE] 이어야 함: " + notation);
            }
            return new ExpectedMapping(columnPart.substring(0, dot), columnPart.substring(dot + 1), code);
        }

        /** 리포트 표기용 문자열 */
        public String display() {
            return physicalTable + "." + physicalColumn + (code == null ? "" : "=" + code);
        }
    }
}
