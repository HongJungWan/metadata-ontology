package com.hris.metadata.domain.term.vo;

import com.hris.metadata.shared.ddd.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * 표준 용어 식별자 값 객체 (타입드 ID).
 * <p>
 * 앱 배정 UUID 를 {@code @EmbeddedId} 로 매핑한다. QueryDSL APT 가 record {@code @Embeddable} 을
 * 지원하지 않아(Illegal type) 기존 값 문자열 VO 와 동일하게 불변 final 클래스로 둔다.
 */
@Embeddable
@ValueObject
@EqualsAndHashCode
public final class TermId {

    private final UUID value;

    protected TermId() {
        this.value = null;
    }

    public TermId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("termId 는 null 일 수 없습니다.");
        }
        this.value = value;
    }

    public UUID value() {
        return value;
    }
}
