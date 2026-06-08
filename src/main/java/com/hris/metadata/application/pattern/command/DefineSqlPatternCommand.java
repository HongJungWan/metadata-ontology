package com.hris.metadata.application.pattern.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hris.metadata.domain.pattern.PatternOperator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * SQL 패턴 등록/수정 커맨드 (DDD 2.3).
 * <p>JSON 필드명은 기존 SqlPatternRequest 와 동일하다(REST 계약 불변).
 */
@Schema(description = "SQL 패턴 요청")
public record DefineSqlPatternCommand(

        @NotBlank(message = "트리거 키워드(triggerKeywords)는 필수입니다.")
        @JsonProperty("triggerKeywords")
        @Schema(description = "트리거 키워드 (콤마 구분)", example = "미정산", requiredMode = Schema.RequiredMode.REQUIRED)
        String triggerKeywords,

        @NotBlank(message = "대상 컬럼(columnTarget)은 필수입니다.")
        @JsonProperty("columnTarget")
        @Schema(description = "대상 컬럼", example = "settlement_status", requiredMode = Schema.RequiredMode.REQUIRED)
        String columnTarget,

        @NotNull(message = "연산자(operator)는 필수입니다.")
        @JsonProperty("operator")
        @Schema(description = "연산자", example = "EQ", requiredMode = Schema.RequiredMode.REQUIRED)
        PatternOperator operator,

        @JsonProperty("valueTemplate")
        @Schema(description = "값 템플릿", example = "PENDING")
        String valueTemplate,

        @JsonProperty("priority")
        @Schema(description = "우선순위 (낮을수록 우선)", example = "1")
        int priority
) {
    public DefineSqlPatternCommand {
        if (triggerKeywords == null || triggerKeywords.isBlank()) {
            throw new IllegalArgumentException("트리거 키워드(triggerKeywords)는 필수입니다.");
        }
        if (columnTarget == null || columnTarget.isBlank()) {
            throw new IllegalArgumentException("대상 컬럼(columnTarget)은 필수입니다.");
        }
        if (operator == null) {
            throw new IllegalArgumentException("연산자(operator)는 필수입니다.");
        }
    }
}
