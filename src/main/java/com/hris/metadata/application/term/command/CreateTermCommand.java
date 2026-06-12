package com.hris.metadata.application.term.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hris.metadata.domain.term.TermStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 표준 용어 등록/수정 커맨드 (DDD 2.3 — 의도를 드러내는 입력 모델).
 * <p>JSON 필드명은 기존 TermRequest 와 동일하다(REST 계약 불변).
 */
@Schema(description = "표준 용어 요청")
public record CreateTermCommand(

        @NotBlank(message = "정식 명칭(canonicalName)은 필수입니다.")
        @JsonProperty("canonicalName")
        @Schema(description = "정식 명칭", example = "정산금액", requiredMode = Schema.RequiredMode.REQUIRED)
        String canonicalName,

        @NotBlank(message = "도메인(domain)은 필수입니다.")
        @JsonProperty("domain")
        @Schema(description = "도메인", example = "settlement")
        String domain,

        @JsonProperty("definition")
        @Schema(description = "용어 정의", example = "가맹점에 지급될 정산 금액")
        String definition,

        @JsonProperty("status")
        @Schema(description = "상태 (기본 DRAFT)", example = "ACTIVE")
        TermStatus status
) {
    public CreateTermCommand {
        if (canonicalName == null || canonicalName.isBlank()) {
            throw new IllegalArgumentException("정식 명칭(canonicalName)은 필수입니다.");
        }
    }
}
