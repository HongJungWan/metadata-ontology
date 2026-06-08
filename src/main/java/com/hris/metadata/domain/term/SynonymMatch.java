package com.hris.metadata.domain.term;

/**
 * 동의어 → 표준 용어 매칭 결과 (읽기 전용 도메인 값).
 * <p>
 * 표면형(surface)에 매칭된 동의어와 그 표준 용어의 정식 명칭(canonicalName)을 담는다.
 * 애그리거트 간 객체 참조 없이 expand/resolve 에서 필요한 정보만 평면화한다.
 *
 * @param surface       원본 표면형
 * @param canonicalName 표준 용어 정식 명칭
 */
public record SynonymMatch(String surface, String canonicalName) {
}
