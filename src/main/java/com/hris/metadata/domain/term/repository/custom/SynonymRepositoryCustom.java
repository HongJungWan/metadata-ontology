package com.hris.metadata.domain.term.repository.custom;

import com.hris.metadata.domain.term.entity.Synonym;

import java.util.Optional;

/**
 * 동의어 QueryDSL 커스텀 리포지토리.
 */
public interface SynonymRepositoryCustom {

    /**
     * 표면형(surface) 으로 동의어를 조회한다 (Term fetch join).
     * 동의어 사전 expand/resolve 에서 사용한다.
     */
    Optional<Synonym> findBySurfaceWithTerm(String surface);
}
