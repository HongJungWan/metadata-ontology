package com.hris.metadata.application.term.dto.response;

import com.hris.metadata.domain.term.Synonym;
import com.hris.metadata.domain.term.SynonymType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 동의어 응답.
 */
@Schema(description = "동의어 응답")
@Getter
@Builder
public class SynonymResponse {

    @Schema(description = "동의어 ID")
    private UUID synonymId;

    @Schema(description = "표준 용어 ID")
    private UUID termId;

    @Schema(description = "표면형 표현", example = "세틀")
    private String surface;

    @Schema(description = "동의어 유형", example = "ABBREVIATION")
    private SynonymType type;

    public static SynonymResponse from(Synonym synonym) {
        return SynonymResponse.builder()
                .synonymId(synonym.getSynonymId())
                .termId(synonym.getTermId())
                .surface(synonym.getSurface())
                .type(synonym.getType())
                .build();
    }
}
