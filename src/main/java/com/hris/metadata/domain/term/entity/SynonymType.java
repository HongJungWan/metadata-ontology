package com.hris.metadata.domain.term.entity;

/**
 * 동의어 표현의 유형.
 */
public enum SynonymType {
    /** 약어 (예: 세틀) */
    ABBREVIATION,
    /** 한영 혼용/영문 (예: settle) */
    KOR_ENG,
    /** 오타 (예: 정상금액) */
    TYPO,
    /** 구어체 (예: 머천트) */
    COLLOQUIAL
}
