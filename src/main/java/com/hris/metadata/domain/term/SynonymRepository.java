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
}
