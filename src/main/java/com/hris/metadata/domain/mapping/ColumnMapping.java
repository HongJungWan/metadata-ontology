package com.hris.metadata.domain.mapping;

import java.util.UUID;

/**
 * 매핑 조회 결과 (읽기 전용 도메인 값).
 * <p>
 * 용어-스키마 매핑과 물리 컬럼 정보를 한 번의 조인으로 평면화한 행.
 * resolve 응답의 columnMappings 를 구성하는 데 사용한다.
 *
 * @param termId         표준 용어 ID
 * @param canonicalName  표준 용어 정식 명칭
 * @param physicalTable  물리 테이블
 * @param physicalColumn 물리 컬럼
 * @param codeValueRule  코드값 규칙 (없으면 null)
 */
public record ColumnMapping(UUID termId, String canonicalName, String physicalTable,
                            String physicalColumn, String codeValueRule) {
}
