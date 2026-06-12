package com.hris.metadata.application.evaluation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 재현율 평가 리포트.
 * <p>
 * 같은 정답셋·같은 resolve 경로에 대해 BASELINE(동의어 확장·기간 정규화 미적용)과
 * FULL(현행 resolve)을 비교한다. 지표 정의:
 * <ul>
 *   <li><b>micro recall</b> = Σ(매칭된 기대 매핑) / Σ(전체 기대 매핑) — 헤드라인 지표</li>
 *   <li><b>macro recall</b> = 질의별 recall 의 평균</li>
 *   <li><b>precision</b> = 기대 매핑에 부합하는 반환 매핑 / 전체 반환 매핑.
 *       정답셋이 기대 매핑의 완전 열거가 아닐 수 있어(다중 매핑 용어) 참고 지표로만 본다.</li>
 *   <li><b>codeValueHitRate</b> = 코드값이 명시된 기대 매핑 중 코드까지 정확히 일치한 비율</li>
 *   <li><b>timeRangeHitRate</b> = 기간 표현 질의 중 timeRange 가 해석된 비율</li>
 *   <li><b>mappedQueryRate</b> = 컬럼 매핑이 1건 이상 반환된 질의 비율 (PRD §7 매핑 커버리지)</li>
 * </ul>
 */
@Schema(description = "재현율 평가 리포트 (BASELINE vs FULL)")
public record EvaluationReportResponse(
        @Schema(description = "평가 질의 수") int queryCount,
        @Schema(description = "동의어·정규화 미적용 결과") ArmResult baseline,
        @Schema(description = "현행 resolve 결과") ArmResult full,
        @Schema(description = "질의별 비교") List<QueryComparison> perQuery) {

    @Schema(description = "평가 대상(arm) 별 집계")
    public record ArmResult(
            @Schema(description = "BASELINE 또는 FULL") String arm,
            @Schema(description = "전체 기대 매핑 수") int goldTotal,
            @Schema(description = "매칭된 기대 매핑 수") int matchedGold,
            @Schema(description = "micro recall") double microRecall,
            @Schema(description = "macro recall (질의별 평균)") double macroRecall,
            @Schema(description = "precision (참고 지표)") double precision,
            @Schema(description = "코드값 적중률") double codeValueHitRate,
            @Schema(description = "기간 인식률") double timeRangeHitRate,
            @Schema(description = "매핑 커버리지 (매핑 1건 이상 질의 비율)") double mappedQueryRate) {
    }

    @Schema(description = "질의별 BASELINE/FULL recall 비교")
    public record QueryComparison(
            @Schema(description = "질의 ID") String queryId,
            @Schema(description = "질의 원문") String query,
            @Schema(description = "BASELINE recall") double baselineRecall,
            @Schema(description = "FULL recall") double fullRecall,
            @Schema(description = "BASELINE 에서 놓친 기대 매핑") List<String> missingInBaseline,
            @Schema(description = "FULL 에서 놓친 기대 매핑") List<String> missingInFull) {
    }
}
