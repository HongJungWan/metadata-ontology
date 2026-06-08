package com.hris.metadata.presentation.resolve.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * /resolve 응답.
 * <p>
 * P1 이 한 번의 호출로 표준용어·동의어확장·컬럼/코드값·기간을 받는다. PRD §4.1 형식을 따른다.
 */
@Schema(description = "질의 해석 결과")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResolveResponse {

    @Schema(description = "정규화된 질의", example = "정산상태 가맹점 2026-05-01~2026-05-31")
    private String normalizedQuery;

    @Schema(description = "매칭된 표준 용어")
    private List<ResolvedTerm> terms;

    @Schema(description = "물리 컬럼 매핑")
    private List<ColumnMapping> columnMappings;

    @Schema(description = "해석된 기간 범위 (없으면 null)")
    private TimeRange timeRange;

    @Schema(description = "매핑에 실패한 키워드")
    private List<String> unmapped;

    public ResolveResponse(String normalizedQuery, List<ResolvedTerm> terms,
                           List<ColumnMapping> columnMappings, TimeRange timeRange, List<String> unmapped) {
        this.normalizedQuery = normalizedQuery;
        this.terms = terms;
        this.columnMappings = columnMappings;
        this.timeRange = timeRange;
        this.unmapped = unmapped;
    }

    /** 매칭된 표준 용어 항목 */
    @Schema(description = "매칭된 표준 용어")
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ResolvedTerm {

        @Schema(description = "표준 용어", example = "정산상태")
        private String canonical;

        @Schema(description = "원본 표면형 (동의어 매칭 시)", example = "세틀상태")
        private String matchedSurface;

        public ResolvedTerm(String canonical, String matchedSurface) {
            this.canonical = canonical;
            this.matchedSurface = matchedSurface;
        }
    }

    /** 물리 컬럼 매핑 항목 */
    @Schema(description = "물리 컬럼 매핑")
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ColumnMapping {

        @Schema(description = "물리 테이블", example = "settlement")
        private String physicalTable;

        @Schema(description = "물리 컬럼", example = "settlement_status")
        private String physicalColumn;

        @Schema(description = "코드값 (코드값 규칙이 있을 때)", example = "PENDING")
        private String codeValue;

        public ColumnMapping(String physicalTable, String physicalColumn, String codeValue) {
            this.physicalTable = physicalTable;
            this.physicalColumn = physicalColumn;
            this.codeValue = codeValue;
        }
    }
}
