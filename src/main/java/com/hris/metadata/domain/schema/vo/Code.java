package com.hris.metadata.domain.schema.vo;

import com.hris.metadata.shared.ddd.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

/** 코드 값 객체 (비공백, 대문자 정규화). */
@Embeddable
@ValueObject
@EqualsAndHashCode
public final class Code {

    private final String value;

    protected Code() {
        this.value = null;
    }

    public Code(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("code 는 공백일 수 없습니다.");
        }
        this.value = value.trim().toUpperCase();
    }

    public String value() {
        return value;
    }
}
