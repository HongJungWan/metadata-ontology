package com.hris.metadata.infrastructure.persistence;

import com.hris.metadata.domain.pattern.SqlPattern;
import com.hris.metadata.domain.pattern.vo.SqlPatternId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * SqlPattern Spring Data JPA 어댑터 (infrastructure).
 */
public interface SqlPatternJpaRepository extends JpaRepository<SqlPattern, SqlPatternId> {
}
