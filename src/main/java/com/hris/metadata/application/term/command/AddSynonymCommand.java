package com.hris.metadata.application.term.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hris.metadata.domain.term.SynonymType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * 동의어 등록 커맨드 (DDD 2.3).
 * <p>JSON 필드명은 기존 SynonymRequest 와 동일하다(REST 계약 불변).
 */
@Schema(description = "동의어 요청")
public record AddSynonymCommand(

        @NotNull(message = "표준 용어 ID(termId)는 필수입니다.")
        @JsonProperty("termId")
        @Schema(description = "표준 용어 ID", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID termId,

        @NotBlank(message = "표면형(surface)은 필수입니다.")
        @JsonProperty("surface")
        @Schema(description = "표면형 표현", example = "세틀", requiredMode = Schema.RequiredMode.REQUIRED)
        String surface,

        @NotNull(message = "동의어 유형(type)은 필수입니다.")
        @JsonProperty("type")
        @Schema(description = "동의어 유형", example = "ABBREVIATION", requiredMode = Schema.RequiredMode.REQUIRED)
        SynonymType type
) {
    public AddSynonymCommand {
        if (termId == null) {
            throw new IllegalArgumentException("표준 용어 ID(termId)는 필수입니다.");
        }
        if (surface == null || surface.isBlank()) {
            throw new IllegalArgumentException("표면형(surface)은 필수입니다.");
        }
        if (type == null) {
            throw new IllegalArgumentException("동의어 유형(type)은 필수입니다.");
        }
    }
}
