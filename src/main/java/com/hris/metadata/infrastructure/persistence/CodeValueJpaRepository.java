package com.hris.metadata.infrastructure.persistence;

import com.hris.metadata.domain.schema.CodeValue;
import com.hris.metadata.domain.schema.CodeValueCandidate;
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
     * 표면형(코드/라벨/동의어)에 해당하는 코드값 후보를 물리 테이블·컬럼과 함께 한 번의 쿼리로 찾는다.
     * <p>
     * SchemaCatalog 와 inner join 하므로 카탈로그가 없거나 소프트 삭제된(@Where) 코드값은 제외된다.
     * synonyms 는 콤마로 join 된 문자열이라 LIKE 로 1차 필터하고, 정확 일치는 호출부에서 재검증한다.
     */
    @Query("select new com.hris.metadata.domain.schema.CodeValueCandidate("
            + "c.code.value, c.label.value, c.synonyms.value, sc.physicalTable.value, sc.physicalColumn.value) "
            + "from CodeValue c join SchemaCatalog sc on sc.schemaCatalogId = c.schemaCatalogId "
            + "where c.code.value = :token or c.label.value = :token "
            + "or c.synonyms.value like concat('%', :token, '%')")
    List<CodeValueCandidate> findCandidatesBySurface(@Param("token") String token);
}
