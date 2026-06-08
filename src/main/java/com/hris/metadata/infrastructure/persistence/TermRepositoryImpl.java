package com.hris.metadata.infrastructure.persistence;

import com.hris.metadata.domain.term.Term;
import com.hris.metadata.domain.term.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * TermRepository 포트의 어댑터 (infrastructure). Spring Data JPA 에 위임한다.
 */
@Repository
@RequiredArgsConstructor
public class TermRepositoryImpl implements TermRepository {

    private final TermJpaRepository jpa;

    @Override
    public Term save(Term term) {
        return jpa.save(term);
    }

    @Override
    public long count() {
        return jpa.count();
    }

    @Override
    public List<Term> findAll() {
        return jpa.findAll();
    }

    @Override
    public Optional<Term> findById(UUID termId) {
        return jpa.findById(termId);
    }

    @Override
    public Optional<Term> findByCanonicalName(String canonicalName) {
        return jpa.findByCanonicalName(canonicalName);
    }

    @Override
    public boolean existsByCanonicalName(String canonicalName) {
        return jpa.existsByCanonicalName(canonicalName);
    }

    @Override
    public List<Term> findAllByDomain(String domain) {
        return jpa.findAllByDomain(domain);
    }
}
