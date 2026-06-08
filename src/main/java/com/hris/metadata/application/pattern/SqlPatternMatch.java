package com.hris.metadata.application.pattern;

import com.hris.metadata.domain.pattern.PatternOperator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * SQL 패턴 매칭 후보.
 */
@Schema(description = "SQL 패턴 매칭 후보")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SqlPatternMatch {

    @Schema(description = "매칭된 키워드", example = "미정산")
    private String keyword;

    @Schema(description = "대상 컬럼", example = "settlement_status")
    private String columnTarget;

    @Schema(description = "연산자", example = "EQ")
    private PatternOperator operator;

    @Schema(description = "값 템플릿", example = "PENDING")
    private String valueTemplate;

    @Schema(description = "우선순위 (낮을수록 우선)", example = "1")
    private int priority;

    public SqlPatternMatch(String keyword, String columnTarget, PatternOperator operator,
                           String valueTemplate, int priority) {
        this.keyword = keyword;
        this.columnTarget = columnTarget;
        this.operator = operator;
        this.valueTemplate = valueTemplate;
        this.priority = priority;
    }
}
