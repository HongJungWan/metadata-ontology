package com.hris.metadata.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 비즈니스 에러 코드 정의.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common (400 / 409 / 500)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    INVALID_STATE(HttpStatus.CONFLICT, "허용되지 않는 상태 전이입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // Term / Synonym (404 / 409)
    TERM_NOT_FOUND(HttpStatus.NOT_FOUND, "표준 용어를 찾을 수 없습니다."),
    DUPLICATE_TERM(HttpStatus.CONFLICT, "이미 존재하는 표준 용어입니다."),
    SYNONYM_NOT_FOUND(HttpStatus.NOT_FOUND, "동의어를 찾을 수 없습니다."),

    // Schema / CodeValue (404)
    SCHEMA_CATALOG_NOT_FOUND(HttpStatus.NOT_FOUND, "물리 스키마 카탈로그를 찾을 수 없습니다."),
    CODE_VALUE_NOT_FOUND(HttpStatus.NOT_FOUND, "코드값을 찾을 수 없습니다."),

    // Mapping (404 / 409)
    MAPPING_NOT_FOUND(HttpStatus.NOT_FOUND, "용어-스키마 매핑을 찾을 수 없습니다."),
    DUPLICATE_MAPPING(HttpStatus.CONFLICT, "이미 존재하는 매핑입니다."),

    // SqlPattern (404)
    SQL_PATTERN_NOT_FOUND(HttpStatus.NOT_FOUND, "SQL 패턴을 찾을 수 없습니다."),

    // Import (400)
    IMPORT_PARSE_FAILED(HttpStatus.BAD_REQUEST, "CSV 임포트 파싱에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
