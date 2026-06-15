package com.hris.metadata.domain.schema.vo;

import com.hris.metadata.shared.ddd.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

/** 물리 컬럼명 값 객체 (비공백). */
@Embeddable
@ValueObject
@EqualsAndHashCode
public final class PhysicalColumn {

    private final String value;

    protected PhysicalColumn() {
        this.value = null;
    }

    public PhysicalColumn(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("physicalColumn 은 공백일 수 없습니다.");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }
}
