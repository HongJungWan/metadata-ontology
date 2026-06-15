package com.hris.metadata.application.term.port;

import com.hris.metadata.domain.term.SynonymType;

import java.util.List;

/**
 * 사전 CSV 소스 ACL (anti-corruption layer) — 아웃바운드 포트.
 * <p>
 * 외부 CSV 텍스트(헤더/공백/컬럼수 변동 포함)를 도메인이 받아들일 수 있는 {@link Row} 목록으로 번역한다:
 * 줄 분해 · 헤더 skip · 컬럼수 검증 · trim · SynonymType 파싱/기본값. 외부 표현의 변화가 응용/도메인으로
 * 새어 들어오지 못하게 막는 경계다. 구현은 infrastructure({@code infrastructure.dictionary.DictionaryCsvSourceAdapter}).
 * (knowledge-search 의 {@code SettlementSourceAcl} 와 동일 패턴.)
 */
public interface DictionaryCsvSource {

    /** CSV 전체 텍스트를 번역된 행 + 구조적 skip 진단으로 파싱한다. */
    Parsed parse(String csv);

    /** 번역된 사전 행(동의어는 없을 수 있음 — surface=null). 도메인 불변식 검증은 팩토리/VO 가 담당. */
    record Row(int rowNumber, String canonicalName, String domain, String definition,
               String synonymSurface, SynonymType synonymType) {

        public boolean hasSynonym() {
            return synonymSurface != null && !synonymSurface.isBlank();
        }
    }

    /** 파싱 결과: 처리 대상 행 + 구조적 사유로 건너뛴 행 진단(컬럼수 불일치 등). */
    record Parsed(List<Row> rows, List<String> skipped) {
    }
}
