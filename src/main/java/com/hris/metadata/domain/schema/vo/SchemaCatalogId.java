package com.hris.metadata.domain.schema.vo;

import com.hris.metadata.shared.ddd.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/** 물리 스키마 카탈로그 식별자 값 객체 (타입드 ID, @EmbeddedId 매핑용 불변 final 클래스). */
@Embeddable
@ValueObject
@EqualsAndHashCode
public final class SchemaCatalogId {

    private final UUID value;

    protected SchemaCatalogId() {
        this.value = null;
    }

    public SchemaCatalogId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("schemaCatalogId 는 null 일 수 없습니다.");
        }
        this.value = value;
    }

    public UUID value() {
        return value;
    }
}
