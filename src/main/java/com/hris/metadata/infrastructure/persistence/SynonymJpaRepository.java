package com.hris.metadata.infrastructure.persistence;

import com.hris.metadata.domain.term.Synonym;
import com.hris.metadata.domain.term.vo.SynonymId;
import com.hris.metadata.domain.term.vo.TermId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Synonym Spring Data JPA 어댑터 (infrastructure).
 */
public interface SynonymJpaRepository extends JpaRepository<Synonym, SynonymId> {

    List<Synonym> findAllByTermId(TermId termId);
}
