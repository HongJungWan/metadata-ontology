package com.hris.metadata.application.resolve.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * /prompt-context 커맨드 (DDD 2.3).
 * <p>
 * query 또는 terms 중 하나를 제공한다. 둘 다 있으면 terms 를 우선한다.
 * JSON 필드명은 기존 PromptContextRequest 와 동일하다(REST 계약 불변).
 */
@Schema(description = "프롬프트 컨텍스트 요청")
public record BuildPromptContextCommand(

        @JsonProperty("query")
        @Schema(description = "자연어 질의", example = "미정산 가맹점")
        String query,

        @JsonProperty("terms")
        @Schema(description = "표준 용어명 목록 (query 대신 직접 지정)", example = "[\"정산상태\", \"가맹점\"]")
        List<String> terms
) {
    public BuildPromptContextCommand {
        // 역직렬화 단계에서 실행 — HttpMessageNotReadableException 으로 감싸여 기존 400 매핑 경로를 탄다.
        if ((query == null || query.isBlank()) && (terms == null || terms.isEmpty())) {
            throw new IllegalArgumentException("query 또는 terms 중 하나는 필요합니다");
        }
    }
}
