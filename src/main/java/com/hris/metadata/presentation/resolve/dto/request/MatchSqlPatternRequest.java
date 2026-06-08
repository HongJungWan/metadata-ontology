package com.hris.metadata.presentation.resolve.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * /match-sql-pattern 요청.
 */
@Schema(description = "SQL 패턴 매칭 요청")
@Getter
@Setter
@NoArgsConstructor
public class MatchSqlPatternRequest {

    @NotNull(message = "키워드(keywords)는 필수입니다.")
    @JsonProperty("keywords")
    @Schema(description = "매칭할 키워드 목록", example = "[\"미정산\", \"수수료 높은\"]",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> keywords;
}
