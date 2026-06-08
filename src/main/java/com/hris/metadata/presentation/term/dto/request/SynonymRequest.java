package com.hris.metadata.presentation.term.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hris.metadata.domain.term.SynonymType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * 동의어 등록/수정 요청.
 */
@Schema(description = "동의어 요청")
@Getter
@Setter
@NoArgsConstructor
public class SynonymRequest {

    @NotNull(message = "표준 용어 ID(termId)는 필수입니다.")
    @JsonProperty("termId")
    @Schema(description = "표준 용어 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID termId;

    @NotBlank(message = "표면형(surface)은 필수입니다.")
    @JsonProperty("surface")
    @Schema(description = "표면형 표현", example = "세틀", requiredMode = Schema.RequiredMode.REQUIRED)
    private String surface;

    @NotNull(message = "동의어 유형(type)은 필수입니다.")
    @JsonProperty("type")
    @Schema(description = "동의어 유형", example = "ABBREVIATION", requiredMode = Schema.RequiredMode.REQUIRED)
    private SynonymType type;
}
