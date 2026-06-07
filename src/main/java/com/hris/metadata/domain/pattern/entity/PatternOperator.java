package com.hris.metadata.domain.pattern.entity;

/**
 * SQL 패턴이 컬럼에 적용하는 연산자.
 */
public enum PatternOperator {
    /** 같음 (=) */
    EQ,
    /** 포함 (IN) */
    IN,
    /** 이상 (>=) */
    GTE,
    /** 이하 (<=) */
    LTE,
    /** 부분 일치 (LIKE) */
    LIKE,
    /** 범위 (BETWEEN) */
    BETWEEN
}
