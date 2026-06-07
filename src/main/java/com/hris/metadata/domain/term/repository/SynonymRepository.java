package com.hris.metadata.domain.term.repository;

import com.hris.metadata.domain.term.entity.Synonym;
import com.hris.metadata.domain.term.repository.custom.SynonymRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * 동의어 리포지토리.
 */
public interface SynonymRepository extends JpaRepository<Synonym, UUID>, SynonymRepositoryCustom {

    List<Synonym> findAllByTermId(UUID termId);
}
