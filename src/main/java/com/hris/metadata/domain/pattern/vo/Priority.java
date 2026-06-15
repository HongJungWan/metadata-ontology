package com.hris.metadata.domain.pattern.vo;

import com.hris.metadata.shared.ddd.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

/**
 * SQL 패턴 우선순위 값 객체 — 0 이상 불변식(낮을수록 우선).
 * final 필드 클래스(QueryDSL 5.1.0 APT record-embeddable 미지원 회피), 접근자 {@code value()}.
 * QueryDSL 은 {@code .priority.value}(NumberPath)로 정렬한다.
 */
@Embeddable
@ValueObject
@EqualsAndHashCode
public final class Priority {

    private final int value;

    protected Priority() {
        this.value = 0;
    }

    public Priority(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("priority 는 0 이상이어야 합니다: " + value);
        }
        this.value = value;
    }

    public int value() {
        return value;
    }
}
