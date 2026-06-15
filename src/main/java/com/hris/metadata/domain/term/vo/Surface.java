package com.hris.metadata.domain.term.vo;

import com.hris.metadata.shared.ddd.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

/** 동의어 표면형 값 객체 (비공백). */
@Embeddable
@ValueObject
@EqualsAndHashCode
public final class Surface {

    private final String value;

    protected Surface() {
        this.value = null;
    }

    public Surface(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("surface 는 공백일 수 없습니다.");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }
}
