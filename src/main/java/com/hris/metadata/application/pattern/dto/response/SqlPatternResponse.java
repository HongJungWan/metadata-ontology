package com.hris.metadata.application.pattern.dto.response;

import com.hris.metadata.domain.pattern.PatternOperator;
import com.hris.metadata.domain.pattern.SqlPattern;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * SQL 패턴 응답.
 */
@Schema(description = "SQL 패턴 응답")
@Getter
@Builder
public class SqlPatternResponse {

    @Schema(description = "SQL 패턴 ID")
    private UUID sqlPatternId;

    @Schema(description = "트리거 키워드 (콤마 구분)", example = "미정산")
    private String triggerKeywords;

    @Schema(description = "대상 컬럼", example = "settlement_status")
    private String columnTarget;

    @Schema(description = "연산자", example = "EQ")
    private PatternOperator operator;

    @Schema(description = "값 템플릿", example = "PENDING")
    private String valueTemplate;

    @Schema(description = "우선순위", example = "1")
    private int priority;

    public static SqlPatternResponse from(SqlPattern pattern) {
        return SqlPatternResponse.builder()
                .sqlPatternId(pattern.getSqlPatternId())
                .triggerKeywords(pattern.getTriggerKeywords().value())
                .columnTarget(pattern.getColumnTarget().value())
                .operator(pattern.getOperator())
                .valueTemplate(pattern.getValueTemplate() == null ? null : pattern.getValueTemplate().value())
                .priority(pattern.getPriority())
                .build();
    }
}
