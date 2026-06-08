package com.hris.metadata.application.resolve.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * /resolve 및 /expand /normalize /prompt-context 공용 질의 요청.
 */
@Schema(description = "질의 요청")
@Getter
@Setter
@NoArgsConstructor
public class ResolveRequest {

    @NotBlank(message = "질의(query)는 필수입니다.")
    @JsonProperty("query")
    @Schema(description = "자연어 질의", example = "미정산 가맹점 지난달", requiredMode = Schema.RequiredMode.REQUIRED)
    private String query;

    @JsonProperty("domain")
    @Schema(description = "도메인 (선택)", example = "settlement")
    private String domain;
}
