package com.hris.metadata.domain.pattern.vo;

import com.hris.metadata.shared.ddd.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

/** 값 템플릿 값 객체 (nullable; 없으면 null, 공백 비-null 은 거부). */
@Embeddable
@ValueObject
@EqualsAndHashCode
public final class ValueTemplate {

    private final String value;

    protected ValueTemplate() {
        this.value = null;
    }

    public ValueTemplate(String value) {
        if (value != null && value.isBlank()) {
            throw new IllegalArgumentException("valueTemplate 은 공백일 수 없습니다(없으면 null).");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }
}
