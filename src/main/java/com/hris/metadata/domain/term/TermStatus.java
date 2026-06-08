package com.hris.metadata.domain.term;

/**
 * 표준 용어의 거버넌스 상태.
 */
public enum TermStatus {
    /** 활성 (검토 완료, 사용 가능) */
    ACTIVE,
    /** 초안 (등록됐으나 검토 전) */
    DRAFT,
    /** 폐기 (더 이상 사용하지 않음) */
    DEPRECATED
}
