package com.hris.metadata.application.term;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * CSV 임포트 결과.
 */
@Schema(description = "CSV 임포트 결과")
@Getter
@Builder
public class ImportResult {

    @Schema(description = "신규 생성된 용어 수", example = "3")
    private int createdTerms;

    @Schema(description = "신규 생성된 동의어 수", example = "5")
    private int createdSynonyms;

    @Schema(description = "건너뛴 행 (이미 존재/오류)")
    private List<String> skipped;
}
