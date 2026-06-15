package com.hris.metadata.domain.schema.vo;

import com.hris.metadata.shared.ddd.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

/** 물리 테이블명 값 객체 (비공백). */
@Embeddable
@ValueObject
@EqualsAndHashCode
public final class PhysicalTable {

    private final String value;

    protected PhysicalTable() {
        this.value = null;
    }

    public PhysicalTable(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("physicalTable 은 공백일 수 없습니다.");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }
}
