package com.hris.metadata.domain.expand;

import com.hris.metadata.shared.ddd.ValueObject;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 동의어 확장 결과.
 */
@ValueObject
@Schema(description = "동의어 확장 결과")
public record ExpansionResult(
        @Schema(description = "동의어를 표준 용어로 치환한 질의", example = "정산상태 가맹점") String expandedQuery,
        @Schema(description = "치환된 토큰 목록") List<TokenExpansion> expansions) {

    /**
     * 개별 토큰 확장 내역.
     */
    @ValueObject
    @Schema(description = "토큰 확장 내역")
    public record TokenExpansion(
            @Schema(description = "원본 표면형", example = "세틀") String surface,
            @Schema(description = "표준 용어", example = "정산") String canonical) {
    }
}
