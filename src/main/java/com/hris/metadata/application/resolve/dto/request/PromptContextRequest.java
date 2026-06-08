package com.hris.metadata.application.resolve.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * /prompt-context 요청.
 * <p>
 * query 또는 terms 중 하나를 제공한다. 둘 다 있으면 terms 를 우선한다.
 */
@Schema(description = "프롬프트 컨텍스트 요청")
@Getter
@Setter
@NoArgsConstructor
public class PromptContextRequest {

    @JsonProperty("query")
    @Schema(description = "자연어 질의", example = "미정산 가맹점")
    private String query;

    @JsonProperty("terms")
    @Schema(description = "표준 용어명 목록 (query 대신 직접 지정)", example = "[\"정산상태\", \"가맹점\"]")
    private List<String> terms;
}
