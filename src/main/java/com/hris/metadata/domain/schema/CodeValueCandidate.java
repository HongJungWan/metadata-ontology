package com.hris.metadata.domain.schema;

/**
 * 표면형 매칭용 코드값 후보 (읽기 전용 도메인 값).
 * <p>
 * 코드값과 그것이 속한 물리 테이블·컬럼을 한 번에 평면화한다.
 * resolve 의 코드값 풀이(예: "미정산" → settlement.settlement_status → PENDING)에서 사용한다.
 *
 * @param code           코드 (예: PENDING)
 * @param label          라벨 (예: 미정산)
 * @param synonyms       코드값 동의어 (콤마 구분, 예: "미정산,대기")
 * @param physicalTable  물리 테이블 (예: settlement)
 * @param physicalColumn 물리 컬럼 (예: settlement_status)
 */
public record CodeValueCandidate(String code, String label, String synonyms,
                                 String physicalTable, String physicalColumn) {
}
