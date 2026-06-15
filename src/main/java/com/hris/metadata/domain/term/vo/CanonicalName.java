package com.hris.metadata.domain.term.vo;

import com.hris.metadata.shared.ddd.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

/** 표준 용어 정식 명칭 값 객체 (비공백). */
@Embeddable
@ValueObject
@EqualsAndHashCode
public final class CanonicalName {

    private final String value;

    protected CanonicalName() {
        this.value = null;
    }

    public CanonicalName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("canonicalName 은 공백일 수 없습니다.");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }
}
