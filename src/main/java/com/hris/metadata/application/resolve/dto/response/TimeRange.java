package com.hris.metadata.application.resolve.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 기간 범위 [from, to] (inclusive).
 */
@Schema(description = "기간 범위")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeRange {

    @Schema(description = "시작일 (포함)", example = "2026-05-01")
    private LocalDate from;

    @Schema(description = "종료일 (포함)", example = "2026-05-31")
    private LocalDate to;

    public TimeRange(LocalDate from, LocalDate to) {
        this.from = from;
        this.to = to;
    }

    public static TimeRange of(LocalDate from, LocalDate to) {
        return new TimeRange(from, to);
    }
}
