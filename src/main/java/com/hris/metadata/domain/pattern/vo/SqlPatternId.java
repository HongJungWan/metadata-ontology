package com.hris.metadata.domain.pattern.vo;

import com.hris.metadata.shared.ddd.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/** SQL 패턴 식별자 값 객체 (타입드 ID, @EmbeddedId 매핑용 불변 final 클래스). */
@Embeddable
@ValueObject
@EqualsAndHashCode
public final class SqlPatternId {

    private final UUID value;

    protected SqlPatternId() {
        this.value = null;
    }

    public SqlPatternId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("sqlPatternId 는 null 일 수 없습니다.");
        }
        this.value = value;
    }

    public UUID value() {
        return value;
    }
}
