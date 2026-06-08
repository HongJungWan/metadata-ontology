package com.hris.metadata.application.normalize;

import com.hris.metadata.application.resolve.dto.response.TimeRange;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 정규화 결과.
 * <p>
 * 한국어 상대 기간 표현을 실제 날짜 범위로 변환한 결과와, 기간 표현을 제거한 잔여 질의를 담는다.
 */
@Schema(description = "정규화 결과")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NormalizationResult {

    @Schema(description = "해석된 기간 범위 (없으면 null)")
    private TimeRange timeRange;

    @Schema(description = "기간 표현을 제거한 잔여 질의", example = "미정산 가맹점")
    private String residualQuery;

    @Schema(description = "매칭된 기간 표현 (없으면 null)", example = "지난달")
    private String matchedExpression;

    public NormalizationResult(TimeRange timeRange, String residualQuery, String matchedExpression) {
        this.timeRange = timeRange;
        this.residualQuery = residualQuery;
        this.matchedExpression = matchedExpression;
    }
}
