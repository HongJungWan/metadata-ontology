package com.hris.metadata.application.expand;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 동의어 확장 결과.
 */
@Schema(description = "동의어 확장 결과")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpansionResult {

    @Schema(description = "동의어를 표준 용어로 치환한 질의", example = "정산상태 가맹점")
    private String expandedQuery;

    @Schema(description = "치환된 토큰 목록")
    private List<TokenExpansion> expansions;

    public ExpansionResult(String expandedQuery, List<TokenExpansion> expansions) {
        this.expandedQuery = expandedQuery;
        this.expansions = expansions;
    }

    /**
     * 개별 토큰 확장 내역.
     */
    @Schema(description = "토큰 확장 내역")
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class TokenExpansion {

        @Schema(description = "원본 표면형", example = "세틀")
        private String surface;

        @Schema(description = "표준 용어", example = "정산")
        private String canonical;

        public TokenExpansion(String surface, String canonical) {
            this.surface = surface;
            this.canonical = canonical;
        }
    }
}
