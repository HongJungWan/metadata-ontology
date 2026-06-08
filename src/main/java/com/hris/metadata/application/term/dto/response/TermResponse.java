package com.hris.metadata.application.term.dto.response;

import com.hris.metadata.domain.term.Term;
import com.hris.metadata.domain.term.TermStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 표준 용어 응답.
 */
@Schema(description = "표준 용어 응답")
@Getter
@Builder
public class TermResponse {

    @Schema(description = "표준 용어 ID")
    private UUID termId;

    @Schema(description = "정식 명칭", example = "정산금액")
    private String canonicalName;

    @Schema(description = "도메인", example = "settlement")
    private String domain;

    @Schema(description = "용어 정의")
    private String definition;

    @Schema(description = "상태", example = "ACTIVE")
    private TermStatus status;

    public static TermResponse from(Term term) {
        return TermResponse.builder()
                .termId(term.getTermId())
                .canonicalName(term.getCanonicalName())
                .domain(term.getDomain())
                .definition(term.getDefinition())
                .status(term.getStatus())
                .build();
    }
}
