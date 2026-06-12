package com.hris.metadata.application.resolve;

/**
 * resolve 파이프라인 단계 토글 (응용 입력 모델).
 * <p>
 * 재현율 평가에서 동의어 확장·기간 정규화 적용 전(baseline)/후(full)를
 * 같은 코드 경로로 비교하기 위해 쓴다. REST 계약에는 노출하지 않는다.
 */
public record ResolveOptions(boolean expandSynonyms, boolean normalizeTime) {

    /** 현행 resolve 동작 그대로 (동의어 확장 + 기간 정규화). */
    public static ResolveOptions full() {
        return new ResolveOptions(true, true);
    }

    /** 원문 토큰을 그대로 용어 조회에 태우는 비교 기준선. */
    public static ResolveOptions rawBaseline() {
        return new ResolveOptions(false, false);
    }
}
