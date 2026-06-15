package com.hris.metadata.domain.mapping;

import com.hris.metadata.shared.ddd.DomainService;

import java.util.Objects;

/**
 * 매핑 매칭 규칙 도메인 서비스.
 * <p>
 * 반환 매핑이 기대 매핑을 충족하는지 판정한다 — 물리 테이블·컬럼 일치 + (기대 코드가 있으면) 코드 일치.
 * 순수 도메인 로직이다(원시값 String 만 다룬다). 빈 등록은
 * {@code infrastructure.config.DomainServiceConfig} 의 {@code @Bean}.
 */
@DomainService
public class MappingComparator {

    /** 반환 매핑이 기대 매핑을 충족하는가: 테이블·컬럼 일치 + (기대 코드가 있으면) 코드 일치. */
    public boolean satisfies(String expectedTable, String expectedColumn, String expectedCode,
                             String actualTable, String actualColumn, String actualCode) {
        return Objects.equals(expectedTable, actualTable)
                && Objects.equals(expectedColumn, actualColumn)
                && (expectedCode == null || Objects.equals(expectedCode, actualCode));
    }
}
