package com.hris.metadata.domain.term;

import com.hris.metadata.domain.term.vo.TermId;

import java.util.List;
import java.util.Optional;

/**
 * 표준 용어 리포지토리 포트 (도메인 소유, 구현은 infrastructure — DIP).
 */
public interface TermRepository {

    Term save(Term term);

    long count();

    List<Term> findAll();

    Optional<Term> findById(TermId termId);

    Optional<Term> findByCanonicalName(String canonicalName);

    boolean existsByCanonicalName(String canonicalName);

    List<Term> findAllByDomain(String domain);
}
