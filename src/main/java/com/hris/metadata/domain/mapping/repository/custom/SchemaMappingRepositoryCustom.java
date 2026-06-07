package com.hris.metadata.domain.mapping.repository.custom;

import com.hris.metadata.domain.mapping.repository.custom.dto.ColumnMappingRow;

import java.util.List;
import java.util.UUID;

/**
 * 용어-스키마 매핑 QueryDSL 커스텀 리포지토리.
 */
public interface SchemaMappingRepositoryCustom {

    /**
     * 표준 용어 ID 목록에 대해 매핑 + 물리 스키마 카탈로그를 한 번에 조회한다.
     * resolve 응답의 columnMappings 를 구성하는 데 사용한다.
     */
    List<ColumnMappingRow> findColumnMappingsByTermIds(List<UUID> termIds);
}
