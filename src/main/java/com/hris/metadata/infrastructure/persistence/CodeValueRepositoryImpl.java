package com.hris.metadata.infrastructure.persistence;

import com.hris.metadata.domain.schema.CodeValue;
import com.hris.metadata.domain.schema.CodeValueCandidate;
import com.hris.metadata.domain.schema.CodeValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * CodeValueRepository 포트의 어댑터 (infrastructure).
 * <p>
 * 표면형 후보 조회는 SchemaCatalog 와 조인한 단일 JPQL 생성자 프로젝션으로 {@link CodeValueCandidate}
 * 를 바로 만든다(N+1 없음). 정확 일치 재검증은 호출부(ResolveService)가 담당한다.
 */
@Repository
@RequiredArgsConstructor
public class CodeValueRepositoryImpl implements CodeValueRepository {

    private final CodeValueJpaRepository jpa;

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
        return jpa.findCandidatesBySurface(token);
    }
}
