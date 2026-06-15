package com.hris.metadata.domain.schema.vo;

import com.hris.metadata.shared.ddd.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/** 코드값 식별자 값 객체 (타입드 ID, @EmbeddedId 매핑용 불변 final 클래스). */
@Embeddable
@ValueObject
@EqualsAndHashCode
public final class CodeValueId {

    private final UUID value;

    protected CodeValueId() {
        this.value = null;
    }

    public CodeValueId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("codeValueId 는 null 일 수 없습니다.");
        }
        this.value = value;
    }

    public UUID value() {
        return value;
    }
}
