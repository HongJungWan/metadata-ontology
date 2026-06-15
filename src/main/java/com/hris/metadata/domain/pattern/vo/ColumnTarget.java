package com.hris.metadata.domain.pattern.vo;

import com.hris.metadata.shared.ddd.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

/** 대상 컬럼 값 객체 (비공백). */
@Embeddable
@ValueObject
@EqualsAndHashCode
public final class ColumnTarget {

    private final String value;

    protected ColumnTarget() {
        this.value = null;
    }

    public ColumnTarget(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("columnTarget 은 공백일 수 없습니다.");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }
}
