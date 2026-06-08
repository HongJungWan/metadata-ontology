package com.hris.metadata.application.normalize;

import com.hris.metadata.application.resolve.dto.response.TimeRange;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 한국어 상대 기간 정규화 서비스 (응용 서비스).
 * <p>
 * "지난달/이번달/지난주/이번주/오늘/어제/최근 N일" 등의 표현을 실제 {from, to} 날짜로 변환한다.
 * 모든 메서드는 기준일(today) 을 인자로 받아 테스트 가능하도록 설계했다.
 * 작성자(author) 정규화는 placeholder 수준의 단순 구현이다.
 */
@Service
public class NormalizationService {

    /** "최근 N일" 패턴 (예: "최근 7일") */
    private static final Pattern RECENT_DAYS = Pattern.compile("최근\\s*(\\d+)\\s*일");

    /**
     * 질의를 정규화한다 (기준일 = 오늘).
     */
    public NormalizationResult normalize(String query) {
        return normalize(query, LocalDate.now());
    }

    /**
     * 질의를 정규화한다 (기준일 명시 — 테스트용).
     *
     * @param query 원본 질의
     * @param today 기준일
     * @return 기간 범위 + 잔여 질의를 담은 결과
     */
    public NormalizationResult normalize(String query, LocalDate today) {
        if (query == null || query.isBlank()) {
            return new NormalizationResult(null, query == null ? "" : query, null);
        }

        // 1. "최근 N일" 우선 처리 (가변 길이 표현)
        Matcher recent = RECENT_DAYS.matcher(query);
        if (recent.find()) {
            int days = Integer.parseInt(recent.group(1));
            TimeRange range = recentDays(today, days);
            String expression = recent.group();
            return new NormalizationResult(range, stripExpression(query, expression), expression);
        }

        // 2. 고정 키워드 매칭 (가장 먼저 등장하는 표현 채택)
        for (String keyword : fixedKeywords()) {
            if (query.contains(keyword)) {
                TimeRange range = resolveFixed(keyword, today);
                return new NormalizationResult(range, stripExpression(query, keyword), keyword);
            }
        }

        // 3. 기간 표현 없음
        return new NormalizationResult(null, query.trim(), null);
    }

    private String[] fixedKeywords() {
        // 더 긴 표현을 먼저 검사해 부분 매칭 오류를 막는다.
        return new String[]{"지난달", "이번달", "저번달", "지난주", "이번주", "올해", "작년", "오늘", "어제"};
    }

    private TimeRange resolveFixed(String keyword, LocalDate today) {
        return switch (keyword) {
            case "지난달", "저번달" -> lastMonth(today);
            case "이번달" -> thisMonth(today);
            case "지난주" -> lastWeek(today);
            case "이번주" -> thisWeek(today);
            case "올해" -> thisYear(today);
            case "작년" -> lastYear(today);
            case "오늘" -> TimeRange.of(today, today);
            case "어제" -> TimeRange.of(today.minusDays(1), today.minusDays(1));
            default -> null;
        };
    }

    private TimeRange lastMonth(LocalDate today) {
        LocalDate firstOfThisMonth = today.withDayOfMonth(1);
        LocalDate lastMonthDay = firstOfThisMonth.minusMonths(1);
        LocalDate from = lastMonthDay.withDayOfMonth(1);
        LocalDate to = lastMonthDay.with(TemporalAdjusters.lastDayOfMonth());
        return TimeRange.of(from, to);
    }

    private TimeRange thisMonth(LocalDate today) {
        LocalDate from = today.withDayOfMonth(1);
        LocalDate to = today.with(TemporalAdjusters.lastDayOfMonth());
        return TimeRange.of(from, to);
    }

    private TimeRange lastWeek(LocalDate today) {
        LocalDate thisMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate from = thisMonday.minusWeeks(1);
        LocalDate to = from.plusDays(6);
        return TimeRange.of(from, to);
    }

    private TimeRange thisWeek(LocalDate today) {
        LocalDate from = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate to = from.plusDays(6);
        return TimeRange.of(from, to);
    }

    private TimeRange thisYear(LocalDate today) {
        return TimeRange.of(today.withDayOfYear(1), today.with(TemporalAdjusters.lastDayOfYear()));
    }

    private TimeRange lastYear(LocalDate today) {
        LocalDate lastYear = today.minusYears(1);
        return TimeRange.of(lastYear.withDayOfYear(1), lastYear.with(TemporalAdjusters.lastDayOfYear()));
    }

    private TimeRange recentDays(LocalDate today, int days) {
        // "최근 N일" = 오늘 포함 N일 (오늘 - (N-1) ~ 오늘)
        int span = Math.max(days, 1);
        return TimeRange.of(today.minusDays(span - 1L), today);
    }

    private String stripExpression(String query, String expression) {
        return query.replace(expression, " ").replaceAll("\\s+", " ").trim();
    }
}
