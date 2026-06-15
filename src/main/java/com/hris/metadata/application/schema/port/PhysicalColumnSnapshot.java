package com.hris.metadata.application.schema.port;

/**
 * 외부 물리 스키마 소스(Glue / Redshift information_schema)에서 가져온 컬럼 한 건의 스냅샷.
 * <p>
 * ACL 번역 산출물 — 외부 소스의 표현을 {@code SchemaCatalog} 와 비교 가능한 우리 쪽 형태로 옮긴 값이다.
 * 외부 모델(Glue Table / JDBC ResultSet)이 도메인으로 새어 들어오지 못하게 막는 경계의 데이터.
 *
 * @param physicalTable  물리 테이블명
 * @param physicalColumn 물리 컬럼명
 * @param dataType       데이터 타입
 * @param description    컬럼 설명 (null 허용)
 * @param sourceSystem   출처 시스템 (예: redshift, glue)
 */
public record PhysicalColumnSnapshot(String physicalTable, String physicalColumn, String dataType,
                                     String description, String sourceSystem) {
}
