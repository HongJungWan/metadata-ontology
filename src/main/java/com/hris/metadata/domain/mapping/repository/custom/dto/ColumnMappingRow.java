package com.hris.metadata.domain.mapping.repository.custom.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.util.UUID;

/**
 * 매핑 조회 결과 행 (QueryDSL Projection).
 * <p>
 * 용어-스키마 매핑과 물리 컬럼 정보를 한 번의 조인으로 평면화한 읽기 전용 행.
 */
public class ColumnMappingRow {

    private final UUID termId;
    private final String canonicalName;
    private final String physicalTable;
    private final String physicalColumn;
    private final String codeValueRule;

    @QueryProjection
    public ColumnMappingRow(UUID termId, String canonicalName, String physicalTable,
                            String physicalColumn, String codeValueRule) {
        this.termId = termId;
        this.canonicalName = canonicalName;
        this.physicalTable = physicalTable;
        this.physicalColumn = physicalColumn;
        this.codeValueRule = codeValueRule;
    }

    public UUID getTermId() {
        return termId;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public String getPhysicalTable() {
        return physicalTable;
    }

    public String getPhysicalColumn() {
        return physicalColumn;
    }

    public String getCodeValueRule() {
        return codeValueRule;
    }
}
