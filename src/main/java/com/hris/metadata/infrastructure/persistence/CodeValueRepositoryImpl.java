package com.hris.metadata.infrastructure.persistence;

import com.hris.metadata.domain.schema.CodeValue;
import com.hris.metadata.domain.schema.CodeValueCandidate;
import com.hris.metadata.domain.schema.CodeValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * CodeValueRepository 포트의 어댑터 (infrastructure).
 * <p>
 * 표면형 후보 조회는 JPA `@Query` 로 1차 필터한 뒤, 각 후보를 그 물리 카탈로그(테이블/컬럼)와 함께
 * 평면화한 {@link CodeValueCandidate} 로 매핑한다.
 */
@Repository
@RequiredArgsConstructor
public class CodeValueRepositoryImpl implements CodeValueRepository {

    private final CodeValueJpaRepository jpa;
    private final SchemaCatalogJpaRepository catalogJpa;

    @Override
    public CodeValue save(CodeValue codeValue) {
        return jpa.save(codeValue);
    }

    @Override
    public Optional<CodeValue> findById(UUID codeValueId) {
        return jpa.findById(codeValueId);
    }

    @Override
    public List<CodeValue> findAllBySchemaCatalogId(UUID schemaCatalogId) {
        return jpa.findAllBySchemaCatalogId(schemaCatalogId);
    }

    @Override
    public List<CodeValueCandidate> findCandidatesBySurface(String token) {
        List<CodeValueCandidate> candidates = new ArrayList<>();
        for (CodeValue codeValue : jpa.findCandidatesBySurface(token)) {
            catalogJpa.findById(codeValue.getSchemaCatalogId()).ifPresent(catalog ->
                    candidates.add(new CodeValueCandidate(
                            codeValue.getCode(),
                            codeValue.getLabel(),
                            codeValue.getSynonyms(),
                            catalog.getPhysicalTable(),
                            catalog.getPhysicalColumn())));
        }
        return candidates;
    }
}
