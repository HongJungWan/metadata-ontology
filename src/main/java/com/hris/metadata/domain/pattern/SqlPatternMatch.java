package com.hris.metadata.domain.pattern;

import com.hris.metadata.shared.ddd.ValueObject;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * SQL 패턴 매칭 후보.
 */
@ValueObject
@Schema(description = "SQL 패턴 매칭 후보")
public record SqlPatternMatch(
        @Schema(description = "매칭된 키워드", example = "미정산") String keyword,
        @Schema(description = "대상 컬럼", example = "settlement_status") String columnTarget,
        @Schema(description = "연산자", example = "EQ") PatternOperator operator,
        @Schema(description = "값 템플릿", example = "PENDING") String valueTemplate,
        @Schema(description = "우선순위 (낮을수록 우선)", example = "1") int priority) {
}
