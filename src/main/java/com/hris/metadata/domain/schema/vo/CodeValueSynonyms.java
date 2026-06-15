package com.hris.metadata.domain.schema.vo;

import com.hris.metadata.shared.ddd.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

/** 코드값 동의어(콤마 구분) 값 객체 (nullable; 없으면 null, 공백 비-null 은 거부). */
@Embeddable
@ValueObject
@EqualsAndHashCode
public final class CodeValueSynonyms {

    private final String value;

    protected CodeValueSynonyms() {
        this.value = null;
    }

    public CodeValueSynonyms(String value) {
        if (value != null && value.isBlank()) {
            throw new IllegalArgumentException("synonyms 는 공백일 수 없습니다(없으면 null).");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }
}
