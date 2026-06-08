package com.hris.metadata.shared.ddd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 서브도메인 분류 마커(전략적 설계). 애그리거트 루트가 어떤 서브도메인에 속하는지 표시한다. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Subdomain {
    SubdomainType value();
}
