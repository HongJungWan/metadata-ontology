package com.hris.metadata.application.resolve.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * /match-sql-pattern 커맨드 (DDD 2.3).
 * <p>JSON 필드명은 기존 MatchSqlPatternRequest 와 동일하다(REST 계약 불변).
 */
@Schema(description = "SQL 패턴 매칭 요청")
public record MatchSqlPatternCommand(

        @NotNull(message = "키워드(keywords)는 필수입니다.")
        @JsonProperty("keywords")
        @Schema(description = "매칭할 키워드 목록", example = "[\"미정산\", \"수수료 높은\"]",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> keywords
) {
    public MatchSqlPatternCommand {
        if (keywords == null) {
            throw new IllegalArgumentException("키워드(keywords)는 필수입니다.");
        }
    }
}
