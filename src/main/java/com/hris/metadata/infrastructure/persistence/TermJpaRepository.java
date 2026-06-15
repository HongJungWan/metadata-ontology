package com.hris.metadata.infrastructure.persistence;

import com.hris.metadata.domain.term.Term;
import com.hris.metadata.domain.term.vo.TermId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Term Spring Data JPA 어댑터 (infrastructure).
 */
public interface TermJpaRepository extends JpaRepository<Term, TermId> {

    Optional<Term> findByCanonicalNameValue(String canonicalName);

    boolean existsByCanonicalNameValue(String canonicalName);

    List<Term> findAllByDomainValue(String domain);
}
