package com.hris.metadata.domain.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 매핑 매칭 규칙 단위 테스트.
 * <p>
 * 충족 = 테이블·컬럼 일치 + (기대 코드가 있으면) 코드 일치. 기대 코드 null 이면 코드 무관.
 */
class MappingComparatorTest {

    private final MappingComparator comparator = new MappingComparator();

    @Test
    @DisplayName("테이블·컬럼 일치 + 기대 코드 없음 → 코드 무관하게 충족")
    void tableColumnMatchWithoutExpectedCode() {
        assertThat(comparator.satisfies("settlement", "status", null, "settlement", "status", null)).isTrue();
        assertThat(comparator.satisfies("settlement", "status", null, "settlement", "status", "PENDING")).isTrue();
    }

    @Test
    @DisplayName("테이블·컬럼·코드 모두 일치 → 충족")
    void allMatch() {
        assertThat(comparator.satisfies("settlement", "status", "PENDING",
                "settlement", "status", "PENDING")).isTrue();
    }

    @Test
    @DisplayName("기대 코드 있는데 반환 코드 불일치 → 미충족")
    void expectedCodeMismatch() {
        assertThat(comparator.satisfies("settlement", "status", "PENDING",
                "settlement", "status", "DONE")).isFalse();
        assertThat(comparator.satisfies("settlement", "status", "PENDING",
                "settlement", "status", null)).isFalse();
    }

    @Test
    @DisplayName("테이블 또는 컬럼 불일치 → 미충족")
    void tableOrColumnMismatch() {
        assertThat(comparator.satisfies("settlement", "status", null, "other", "status", null)).isFalse();
        assertThat(comparator.satisfies("settlement", "status", null, "settlement", "amount", null)).isFalse();
    }
}
