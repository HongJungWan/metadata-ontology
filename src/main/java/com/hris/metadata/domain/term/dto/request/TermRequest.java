package com.hris.metadata.domain.term.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hris.metadata.domain.term.entity.TermStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 표준 용어 등록/수정 요청.
 */
@Schema(description = "표준 용어 요청")
@Getter
@Setter
@NoArgsConstructor
public class TermRequest {

    @NotBlank(message = "정식 명칭(canonicalName)은 필수입니다.")
    @JsonProperty("canonicalName")
    @Schema(description = "정식 명칭", example = "정산금액", requiredMode = Schema.RequiredMode.REQUIRED)
    private String canonicalName;

    @JsonProperty("domain")
    @Schema(description = "도메인", example = "settlement")
    private String domain;

    @JsonProperty("definition")
    @Schema(description = "용어 정의", example = "가맹점에 지급될 정산 금액")
    private String definition;

    @JsonProperty("status")
    @Schema(description = "상태 (기본 DRAFT)", example = "ACTIVE")
    private TermStatus status;
}
