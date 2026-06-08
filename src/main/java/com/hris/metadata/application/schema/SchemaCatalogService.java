package com.hris.metadata.application.schema;

import com.hris.metadata.domain.schema.CodeValue;
import com.hris.metadata.domain.schema.CodeValueRepository;
import com.hris.metadata.domain.schema.SchemaCatalog;
import com.hris.metadata.domain.schema.SchemaCatalogRepository;
import com.hris.metadata.global.exception.BusinessException;
import com.hris.metadata.global.exception.ErrorCode;
import com.hris.metadata.presentation.schema.dto.request.CodeValueRequest;
import com.hris.metadata.presentation.schema.dto.request.SchemaCatalogRequest;
import com.hris.metadata.presentation.schema.dto.response.CodeValueResponse;
import com.hris.metadata.presentation.schema.dto.response.SchemaCatalogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 물리 스키마 카탈로그·코드값 관리 서비스 (응용 서비스).
 * <p>
 * 운영에서는 Glue/Redshift 동기화 배치가 카탈로그를 갱신하지만(현재 비활성),
 * 관리자가 수동으로 등록·수정할 수 있는 CRUD 도 제공한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SchemaCatalogService {

    private final SchemaCatalogRepository schemaCatalogRepository;
    private final CodeValueRepository codeValueRepository;

    // ===== SchemaCatalog =====

    @Transactional
    public SchemaCatalogResponse createCatalog(SchemaCatalogRequest request) {
        SchemaCatalog catalog = SchemaCatalog.builder()
                .schemaCatalogId(UUID.randomUUID())
                .physicalTable(request.getPhysicalTable())
                .physicalColumn(request.getPhysicalColumn())
                .dataType(request.getDataType())
                .description(request.getDescription())
                .sourceSystem(request.getSourceSystem())
                .build();
        schemaCatalogRepository.save(catalog);
        return SchemaCatalogResponse.from(catalog);
    }

    public List<SchemaCatalogResponse> getCatalogs() {
        return schemaCatalogRepository.findAll().stream().map(SchemaCatalogResponse::from).toList();
    }

    @Transactional
    public SchemaCatalogResponse updateCatalog(UUID schemaCatalogId, SchemaCatalogRequest request) {
        SchemaCatalog catalog = getCatalogOrThrow(schemaCatalogId);
        catalog.update(request.getPhysicalTable(), request.getPhysicalColumn(), request.getDataType(),
                request.getDescription(), request.getSourceSystem());
        return SchemaCatalogResponse.from(catalog);
    }

    @Transactional
    public void deleteCatalog(UUID schemaCatalogId) {
        getCatalogOrThrow(schemaCatalogId).softDelete();
    }

    // ===== CodeValue =====

    @Transactional
    public CodeValueResponse createCodeValue(CodeValueRequest request) {
        getCatalogOrThrow(request.getSchemaCatalogId());
        CodeValue codeValue = CodeValue.builder()
                .codeValueId(UUID.randomUUID())
                .schemaCatalogId(request.getSchemaCatalogId())
                .code(request.getCode())
                .label(request.getLabel())
                .synonyms(request.getSynonyms())
                .build();
        codeValueRepository.save(codeValue);
        return CodeValueResponse.from(codeValue);
    }

    public List<CodeValueResponse> getCodeValues(UUID schemaCatalogId) {
        return codeValueRepository.findAllBySchemaCatalogId(schemaCatalogId).stream()
                .map(CodeValueResponse::from).toList();
    }

    @Transactional
    public void deleteCodeValue(UUID codeValueId) {
        CodeValue codeValue = codeValueRepository.findById(codeValueId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CODE_VALUE_NOT_FOUND));
        codeValue.softDelete();
    }

    private SchemaCatalog getCatalogOrThrow(UUID schemaCatalogId) {
        return schemaCatalogRepository.findById(schemaCatalogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEMA_CATALOG_NOT_FOUND));
    }
}
