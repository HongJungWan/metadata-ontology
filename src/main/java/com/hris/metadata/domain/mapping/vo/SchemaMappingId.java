package com.hris.metadata.domain.mapping.vo;

import com.hris.metadata.shared.ddd.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/** 용어-스키마 매핑 식별자 값 객체 (타입드 ID, @EmbeddedId 매핑용 불변 final 클래스). */
@Embeddable
@ValueObject
@EqualsAndHashCode
public final class SchemaMappingId {

    private final UUID value;

    protected SchemaMappingId() {
        this.value = null;
    }

    public SchemaMappingId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("schemaMappingId 는 null 일 수 없습니다.");
        }
        this.value = value;
    }

    public UUID value() {
        return value;
    }
}
