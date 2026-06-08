package com.hris.metadata.application.term;

import com.hris.metadata.domain.mapping.SchemaMapping;
import com.hris.metadata.domain.mapping.SchemaMappingRepository;
import com.hris.metadata.domain.schema.SchemaCatalogRepository;
import com.hris.metadata.domain.term.Synonym;
import com.hris.metadata.domain.term.SynonymRepository;
import com.hris.metadata.domain.term.Term;
import com.hris.metadata.domain.term.TermRepository;
import com.hris.metadata.domain.term.TermStatus;
import com.hris.metadata.global.exception.BusinessException;
import com.hris.metadata.global.exception.ErrorCode;
import com.hris.metadata.presentation.term.dto.request.SchemaMappingRequest;
import com.hris.metadata.presentation.term.dto.request.SynonymRequest;
import com.hris.metadata.presentation.term.dto.request.TermRequest;
import com.hris.metadata.presentation.term.dto.response.SchemaMappingResponse;
import com.hris.metadata.presentation.term.dto.response.SynonymResponse;
import com.hris.metadata.presentation.term.dto.response.TermResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 사전(용어/동의어/매핑) 관리 서비스 (응용 서비스).
 * <p>
 * 등록·수정·삭제(소프트삭제) 와 상태 승인(검토 후 활성화) 을 담당한다 (PRD §5).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermAdminService {

    private final TermRepository termRepository;
    private final SynonymRepository synonymRepository;
    private final SchemaMappingRepository schemaMappingRepository;
    private final SchemaCatalogRepository schemaCatalogRepository;

    // ===== Term =====

    @Transactional
    public TermResponse createTerm(TermRequest request) {
        if (termRepository.existsByCanonicalName(request.getCanonicalName())) {
            throw new BusinessException(ErrorCode.DUPLICATE_TERM);
        }
        Term term = Term.builder()
                .termId(UUID.randomUUID())
                .canonicalName(request.getCanonicalName())
                .domain(request.getDomain())
                .definition(request.getDefinition())
                .status(request.getStatus() == null ? TermStatus.DRAFT : request.getStatus())
                .build();
        termRepository.save(term);
        return TermResponse.from(term);
    }

    public List<TermResponse> getTerms() {
        return termRepository.findAll().stream().map(TermResponse::from).toList();
    }

    public TermResponse getTerm(UUID termId) {
        return TermResponse.from(getTermOrThrow(termId));
    }

    @Transactional
    public TermResponse updateTerm(UUID termId, TermRequest request) {
        Term term = getTermOrThrow(termId);
        TermStatus status = request.getStatus() == null ? term.getStatus() : request.getStatus();
        term.update(request.getCanonicalName(), request.getDomain(), request.getDefinition(), status);
        return TermResponse.from(term);
    }

    @Transactional
    public TermResponse approveTerm(UUID termId) {
        Term term = getTermOrThrow(termId);
        term.changeStatus(TermStatus.ACTIVE);
        return TermResponse.from(term);
    }

    @Transactional
    public void deleteTerm(UUID termId) {
        getTermOrThrow(termId).softDelete();
    }

    // ===== Synonym =====

    @Transactional
    public SynonymResponse createSynonym(SynonymRequest request) {
        getTermOrThrow(request.getTermId());
        Synonym synonym = Synonym.builder()
                .synonymId(UUID.randomUUID())
                .termId(request.getTermId())
                .surface(request.getSurface())
                .type(request.getType())
                .build();
        synonymRepository.save(synonym);
        return SynonymResponse.from(synonym);
    }

    public List<SynonymResponse> getSynonyms(UUID termId) {
        return synonymRepository.findAllByTermId(termId).stream().map(SynonymResponse::from).toList();
    }

    @Transactional
    public void deleteSynonym(UUID synonymId) {
        Synonym synonym = synonymRepository.findById(synonymId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SYNONYM_NOT_FOUND));
        synonym.softDelete();
    }

    // ===== Mapping =====

    @Transactional
    public SchemaMappingResponse createMapping(SchemaMappingRequest request) {
        getTermOrThrow(request.getTermId());
        schemaCatalogRepository.findById(request.getSchemaCatalogId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEMA_CATALOG_NOT_FOUND));
        if (schemaMappingRepository.existsByTermIdAndSchemaCatalogId(
                request.getTermId(), request.getSchemaCatalogId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_MAPPING);
        }
        SchemaMapping mapping = SchemaMapping.builder()
                .schemaMappingId(UUID.randomUUID())
                .termId(request.getTermId())
                .schemaCatalogId(request.getSchemaCatalogId())
                .mappingType(request.getMappingType())
                .codeValueRule(request.getCodeValueRule())
                .build();
        schemaMappingRepository.save(mapping);
        return SchemaMappingResponse.from(mapping);
    }

    public List<SchemaMappingResponse> getMappingsByTerm(UUID termId) {
        return schemaMappingRepository.findAllByTermId(termId).stream()
                .map(SchemaMappingResponse::from).toList();
    }

    @Transactional
    public void deleteMapping(UUID schemaMappingId) {
        SchemaMapping mapping = schemaMappingRepository.findById(schemaMappingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MAPPING_NOT_FOUND));
        mapping.softDelete();
    }

    private Term getTermOrThrow(UUID termId) {
        return termRepository.findById(termId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TERM_NOT_FOUND));
    }
}
