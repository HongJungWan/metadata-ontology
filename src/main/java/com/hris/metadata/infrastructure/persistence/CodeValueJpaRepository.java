package com.hris.metadata.infrastructure.persistence;

import com.hris.metadata.domain.schema.CodeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * CodeValue Spring Data JPA 어댑터 (infrastructure).
 */
public interface CodeValueJpaRepository extends JpaRepository<CodeValue, UUID> {

    List<CodeValue> findAllBySchemaCatalogId(UUID schemaCatalogId);

    /**
     * 표면형(코드/라벨/동의어)에 해당하는 코드값 후보를 찾는다.
     * synonyms 는 콤마로 join 된 문자열이라 LIKE 로 1차 필터하고, 정확 일치는 호출부에서 재검증한다.
     */
    @Query("select c from CodeValue c "
            + "where c.code = :token or c.label = :token or c.synonyms like concat('%', :token, '%')")
    List<CodeValue> findCandidatesBySurface(@Param("token") String token);
}
