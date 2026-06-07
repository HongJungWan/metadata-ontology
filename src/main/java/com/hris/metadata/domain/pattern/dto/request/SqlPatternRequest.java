package com.hris.metadata.domain.pattern.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hris.metadata.domain.pattern.entity.PatternOperator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SQL 패턴 등록/수정 요청.
 */
@Schema(description = "SQL 패턴 요청")
@Getter
@Setter
@NoArgsConstructor
public class SqlPatternRequest {

    @NotBlank(message = "트리거 키워드(triggerKeywords)는 필수입니다.")
    @JsonProperty("triggerKeywords")
    @Schema(description = "트리거 키워드 (콤마 구분)", example = "미정산", requiredMode = Schema.RequiredMode.REQUIRED)
    private String triggerKeywords;

    @NotBlank(message = "대상 컬럼(columnTarget)은 필수입니다.")
    @JsonProperty("columnTarget")
    @Schema(description = "대상 컬럼", example = "settlement_status", requiredMode = Schema.RequiredMode.REQUIRED)
    private String columnTarget;

    @NotNull(message = "연산자(operator)는 필수입니다.")
    @JsonProperty("operator")
    @Schema(description = "연산자", example = "EQ", requiredMode = Schema.RequiredMode.REQUIRED)
    private PatternOperator operator;

    @JsonProperty("valueTemplate")
    @Schema(description = "값 템플릿", example = "PENDING")
    private String valueTemplate;

    @JsonProperty("priority")
    @Schema(description = "우선순위 (낮을수록 우선)", example = "1")
    private int priority;
}
