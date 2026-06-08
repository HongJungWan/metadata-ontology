package com.hris.metadata.shared.ddd;

/**
 * 서브도메인 분류 (전략적 설계).
 * <p>
 * CORE: 경쟁 우위의 핵심(용어 표준화·동의어). SUPPORTING: 핵심을 받치는 보조(스키마/코드값/매핑).
 * GENERIC: 범용 규칙(SQL 패턴 — 외부로 대체 가능).
 */
public enum SubdomainType {
    /** 핵심 서브도메인 — 비즈니스 차별화의 본질. */
    CORE,
    /** 지원 서브도메인 — 핵심을 보조. */
    SUPPORTING,
    /** 일반 서브도메인 — 범용/대체 가능. */
    GENERIC
}
