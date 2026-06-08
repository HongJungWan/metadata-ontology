package com.hris.metadata.infrastructure.persistence;

import com.hris.metadata.domain.term.Synonym;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Synonym Spring Data JPA 어댑터 (infrastructure).
 */
public interface SynonymJpaRepository extends JpaRepository<Synonym, UUID> {

    List<Synonym> findAllByTermId(UUID termId);
}
