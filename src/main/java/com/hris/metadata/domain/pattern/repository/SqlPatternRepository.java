package com.hris.metadata.domain.pattern.repository;

import com.hris.metadata.domain.pattern.entity.SqlPattern;
import com.hris.metadata.domain.pattern.repository.custom.SqlPatternRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * SQL 패턴 리포지토리.
 */
public interface SqlPatternRepository extends JpaRepository<SqlPattern, UUID>, SqlPatternRepositoryCustom {
}
