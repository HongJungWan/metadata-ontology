package com.hris.metadata.domain.schema.vo;

import com.hris.metadata.shared.ddd.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

/** 컬럼 설명 값 객체 (nullable; 없으면 null, 공백 비-null 은 거부). */
@Embeddable
@ValueObject
@EqualsAndHashCode
public final class Description {

    private final String value;

    protected Description() {
        this.value = null;
    }

    public Description(String value) {
        if (value != null && value.isBlank()) {
            throw new IllegalArgumentException("description 은 공백일 수 없습니다(없으면 null).");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }
}
