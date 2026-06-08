package com.hris.metadata.application.resolve.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * /resolve 및 /expand /normalize 공용 질의 커맨드 (DDD 2.3).
 * <p>JSON 필드명은 기존 ResolveRequest 와 동일하다(REST 계약 불변).
 */
@Schema(description = "질의 요청")
public record ResolveQueryCommand(

        @NotBlank(message = "질의(query)는 필수입니다.")
        @JsonProperty("query")
        @Schema(description = "자연어 질의", example = "미정산 가맹점 지난달", requiredMode = Schema.RequiredMode.REQUIRED)
        String query,

        @JsonProperty("domain")
        @Schema(description = "도메인 (선택)", example = "settlement")
        String domain
) {
    public ResolveQueryCommand {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("질의(query)는 필수입니다.");
        }
    }
}
