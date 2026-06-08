package com.hris.metadata.infrastructure.persistence;

import com.hris.metadata.domain.pattern.SqlPattern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * SqlPattern Spring Data JPA 어댑터 (infrastructure).
 */
public interface SqlPatternJpaRepository extends JpaRepository<SqlPattern, UUID> {
}
