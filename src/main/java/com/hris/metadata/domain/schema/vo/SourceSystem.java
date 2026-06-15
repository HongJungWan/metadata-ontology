package com.hris.metadata.domain.schema.vo;

import com.hris.metadata.shared.ddd.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

/** 출처 시스템 값 객체 (nullable; 없으면 null, 공백 비-null 은 거부). */
@Embeddable
@ValueObject
@EqualsAndHashCode
public final class SourceSystem {

    private final String value;

    protected SourceSystem() {
        this.value = null;
    }

    public SourceSystem(String value) {
        if (value != null && value.isBlank()) {
            throw new IllegalArgumentException("sourceSystem 은 공백일 수 없습니다(없으면 null).");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }
}
