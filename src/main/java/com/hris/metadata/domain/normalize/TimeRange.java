package com.hris.metadata.domain.normalize;

import com.hris.metadata.shared.ddd.ValueObject;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * 기간 범위 [from, to] (inclusive).
 */
@ValueObject
@Schema(description = "기간 범위")
public record TimeRange(
        @Schema(description = "시작일 (포함)", example = "2026-05-01") LocalDate from,
        @Schema(description = "종료일 (포함)", example = "2026-05-31") LocalDate to) {

    public static TimeRange of(LocalDate from, LocalDate to) {
        return new TimeRange(from, to);
    }
}
