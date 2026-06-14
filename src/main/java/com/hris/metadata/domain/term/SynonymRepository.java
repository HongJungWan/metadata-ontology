package com.hris.metadata.domain.term;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 동의어 리포지토리 포트 (도메인 소유, 구현은 infrastructure — DIP).
 */
public interface SynonymRepository {

    Synonym save(Synonym synonym);

    long count();

    Optional<Synonym> findById(UUID synonymId);

    List<Synonym> findAllByTermId(UUID termId);

    /**
     * 표면형(surface) 으로 동의어를 조회해 표준 용어 정식 명칭과 함께 돌려준다.
     * 동의어 사전 expand/resolve 에서 사용한다.
     */
    Optional<SynonymMatch> findBySurfaceWithTerm(String surface);

    /**
     * 표면형의 <b>퍼지(근사)</b> 매칭 — 정확 매칭이 실패한 토큰(오타·띄어쓰기·근접 OOV)에 대한 폴백.
     * 트라이그램 유사도가 {@code threshold} 이상인 동의어 중 최상위를 표준 용어와 함께 돌려준다.
     * <p>
     * 운영(postgres)은 pg_trgm {@code similarity()}(GIN 인덱스)로, 로컬/CI(H2)는 동일 의미의 Java 트라이그램
     * 유사도로 구현해 결정론적으로 측정 가능하게 한다. 짧은 토큰의 어휘적 미스를 설명 가능하게 회복한다
     * (의미 패러프레이즈 리콜은 knowledge-search 의 본문 벡터가 담당 — 비대칭 설계).
     *
     * @param surface   매칭 대상 표면형(질의 토큰)
     * @param threshold 트라이그램 유사도 임계값(0~1, pg_trgm 기본 0.3)
     */
    Optional<SynonymMatch> findBySurfaceFuzzy(String surface, double threshold);
}
