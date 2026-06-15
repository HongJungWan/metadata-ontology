package com.hris.metadata.domain.term.vo;

import com.hris.metadata.shared.ddd.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

/** 용어 도메인 값 객체 (비공백). */
@Embeddable
@ValueObject
@EqualsAndHashCode
public final class TermDomain {

    private final String value;

    protected TermDomain() {
        this.value = null;
    }

    public TermDomain(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("domain 은 공백일 수 없습니다.");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }
}
