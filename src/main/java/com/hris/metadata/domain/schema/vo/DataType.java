package com.hris.metadata.domain.schema.vo;

import com.hris.metadata.shared.ddd.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

/** 데이터 타입 값 객체 (nullable; 없으면 null, 공백 비-null 은 거부). */
@Embeddable
@ValueObject
@EqualsAndHashCode
public final class DataType {

    private final String value;

    protected DataType() {
        this.value = null;
    }

    public DataType(String value) {
        if (value != null && value.isBlank()) {
            throw new IllegalArgumentException("dataType 는 공백일 수 없습니다(없으면 null).");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }
}
