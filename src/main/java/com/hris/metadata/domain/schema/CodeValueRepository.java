package com.hris.metadata.domain.schema;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 코드값 사전 리포지토리 포트 (도메인 소유, 구현은 infrastructure — DIP).
 */
public interface CodeValueRepository {

    CodeValue save(CodeValue codeValue);

    Optional<CodeValue> findById(UUID codeValueId);

    List<CodeValue> findAllBySchemaCatalogId(UUID schemaCatalogId);

    /**
     * 표면형(코드/라벨/동의어)에 해당하는 코드값 후보를 물리 테이블·컬럼과 함께 찾는다.
     * synonyms 는 콤마로 join 된 문자열이라 LIKE 로 1차 필터하고, 정확 일치는 호출부에서 재검증한다.
     */
    List<CodeValueCandidate> findCandidatesBySurface(String token);
}
