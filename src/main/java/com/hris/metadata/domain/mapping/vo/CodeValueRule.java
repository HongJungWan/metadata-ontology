package com.hris.metadata.domain.mapping.vo;

import com.hris.metadata.shared.ddd.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

/** 코드값 규칙 값 객체 (nullable; 없으면 null, 공백 비-null 은 거부). */
@Embeddable
@ValueObject
@EqualsAndHashCode
public final class CodeValueRule {

    private final String value;

    protected CodeValueRule() {
        this.value = null;
    }

    public CodeValueRule(String value) {
        if (value != null && value.isBlank()) {
            throw new IllegalArgumentException("codeValueRule 은 공백일 수 없습니다(없으면 null).");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }
}
