package com.hris.metadata.domain.term.repository;

import com.hris.metadata.domain.term.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 표준 용어 리포지토리.
 */
public interface TermRepository extends JpaRepository<Term, UUID> {

    Optional<Term> findByCanonicalName(String canonicalName);

    boolean existsByCanonicalName(String canonicalName);

    List<Term> findAllByDomain(String domain);
}
