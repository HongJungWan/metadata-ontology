package com.hris.metadata.domain.pattern.vo;

import com.hris.metadata.shared.ddd.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

/** 트리거 키워드(콤마 구분) 값 객체 (비공백). */
@Embeddable
@ValueObject
@EqualsAndHashCode
public final class TriggerKeywords {

    private final String value;

    protected TriggerKeywords() {
        this.value = null;
    }

    public TriggerKeywords(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("triggerKeywords 는 공백일 수 없습니다.");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }
}
