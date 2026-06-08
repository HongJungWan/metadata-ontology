package com.hris.metadata.application.normalize;

import com.hris.metadata.application.resolve.dto.response.TimeRange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NormalizationService 상대 기간 파싱 단위 테스트 (Spring 컨텍스트 없음).
 */
class NormalizationServiceTest {

    private final NormalizationService service = new NormalizationService();

    @Test
    @DisplayName("'지난달'은 기준일이 속한 달의 직전 달 1일~말일로 변환된다")
    void lastMonth() {
        LocalDate today = LocalDate.of(2026, 6, 7);

        NormalizationResult result = service.normalize("미정산 가맹점 지난달", today);

        TimeRange range = result.getTimeRange();
        assertThat(range).isNotNull();
        assertThat(range.getFrom()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(range.getTo()).isEqualTo(LocalDate.of(2026, 5, 31));
        assertThat(result.getMatchedExpression()).isEqualTo("지난달");
        assertThat(result.getResidualQuery()).isEqualTo("미정산 가맹점");
    }

    @Test
    @DisplayName("'지난달'은 1월 기준일이면 전년도 12월로 변환된다 (연도 경계)")
    void lastMonthAcrossYear() {
        LocalDate today = LocalDate.of(2026, 1, 15);

        NormalizationResult result = service.normalize("지난달 정산", today);

        assertThat(result.getTimeRange().getFrom()).isEqualTo(LocalDate.of(2025, 12, 1));
        assertThat(result.getTimeRange().getTo()).isEqualTo(LocalDate.of(2025, 12, 31));
    }

    @Test
    @DisplayName("'이번달'은 기준일이 속한 달의 1일~말일로 변환된다")
    void thisMonth() {
        LocalDate today = LocalDate.of(2026, 2, 10);

        NormalizationResult result = service.normalize("이번달 수수료", today);

        assertThat(result.getTimeRange().getFrom()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(result.getTimeRange().getTo()).isEqualTo(LocalDate.of(2026, 2, 28));
    }

    @Test
    @DisplayName("'최근 7일'은 오늘 포함 7일 범위로 변환된다")
    void recentDays() {
        LocalDate today = LocalDate.of(2026, 6, 7);

        NormalizationResult result = service.normalize("최근 7일 정산", today);

        assertThat(result.getTimeRange().getFrom()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(result.getTimeRange().getTo()).isEqualTo(LocalDate.of(2026, 6, 7));
    }

    @Test
    @DisplayName("'지난주'는 직전 주 월요일~일요일로 변환된다")
    void lastWeek() {
        // 2026-06-07 은 일요일 → 이번 주 월요일 2026-06-01, 지난주 월요일 2026-05-25
        LocalDate today = LocalDate.of(2026, 6, 7);

        NormalizationResult result = service.normalize("지난주 정산", today);

        assertThat(result.getTimeRange().getFrom()).isEqualTo(LocalDate.of(2026, 5, 25));
        assertThat(result.getTimeRange().getTo()).isEqualTo(LocalDate.of(2026, 5, 31));
    }

    @Test
    @DisplayName("기간 표현이 없으면 timeRange 는 null 이고 원본 질의가 잔여로 남는다")
    void noExpression() {
        NormalizationResult result = service.normalize("미정산 가맹점", LocalDate.of(2026, 6, 7));

        assertThat(result.getTimeRange()).isNull();
        assertThat(result.getResidualQuery()).isEqualTo("미정산 가맹점");
    }
}
