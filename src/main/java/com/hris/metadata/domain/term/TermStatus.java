package com.hris.metadata.domain.term;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 표준 용어의 거버넌스 상태.
 * <p>
 * 상태 전이는 {@link #canTransitionTo(TermStatus)} 로 강제된다(라이프사이클 불변식).
 * 허용 전이:
 * <ul>
 *   <li>DRAFT → ACTIVE (검토 승인), DRAFT → DEPRECATED (검토 없이 폐기)</li>
 *   <li>ACTIVE → DEPRECATED (사용 중단), ACTIVE → DRAFT (활성 초기상태를 초안으로 되돌림 — 등록/임포트 흐름)</li>
 *   <li>같은 상태로의 전이(멱등)는 항상 허용</li>
 * </ul>
 * 그 외(예: DEPRECATED → ACTIVE/DRAFT 부활)는 금지한다.
 */
public enum TermStatus {
    /** 활성 (검토 완료, 사용 가능) */
    ACTIVE,
    /** 초안 (등록됐으나 검토 전) */
    DRAFT,
    /** 폐기 (더 이상 사용하지 않음) */
    DEPRECATED;

    /** 상태별 허용 대상 집합 (같은 상태로의 멱등 전이는 별도 허용). */
    private static final Map<TermStatus, Set<TermStatus>> ALLOWED = Map.of(
            DRAFT, EnumSet.of(ACTIVE, DEPRECATED),
            ACTIVE, EnumSet.of(DRAFT, DEPRECATED),
            DEPRECATED, EnumSet.noneOf(TermStatus.class)
    );

    /**
     * 이 상태에서 {@code target} 상태로 전이할 수 있는지 여부.
     * 같은 상태로의 전이(멱등)는 항상 허용한다.
     */
    public boolean canTransitionTo(TermStatus target) {
        if (target == null) {
            return false;
        }
        if (this == target) {
            return true;
        }
        return ALLOWED.getOrDefault(this, EnumSet.noneOf(TermStatus.class)).contains(target);
    }
}
