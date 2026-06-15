package com.hris.metadata.application.term;

import com.hris.metadata.domain.mapping.SchemaMapping;
import com.hris.metadata.domain.mapping.SchemaMappingRepository;
import com.hris.metadata.domain.schema.SchemaCatalogRepository;
import com.hris.metadata.domain.term.Synonym;
import com.hris.metadata.domain.term.SynonymRepository;
import com.hris.metadata.domain.term.Term;
import com.hris.metadata.domain.term.TermRepository;
import com.hris.metadata.domain.term.TermStatus;
import com.hris.metadata.domain.term.vo.SynonymId;
import com.hris.metadata.domain.term.vo.TermId;
import com.hris.metadata.domain.schema.vo.SchemaCatalogId;
import com.hris.metadata.domain.mapping.vo.SchemaMappingId;
import com.hris.metadata.global.exception.BusinessException;
import com.hris.metadata.global.exception.ErrorCode;
import com.hris.metadata.application.term.command.AddSynonymCommand;
import com.hris.metadata.application.term.command.CreateTermCommand;
import com.hris.metadata.application.term.command.MapTermToColumnCommand;
import com.hris.metadata.application.term.dto.response.SchemaMappingResponse;
import com.hris.metadata.application.term.dto.response.SynonymResponse;
import com.hris.metadata.application.term.dto.response.TermResponse;
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
    public TermResponse createTerm(CreateTermCommand command) {
        if (termRepository.existsByCanonicalName(command.canonicalName())) {
            throw new BusinessException(ErrorCode.DUPLICATE_TERM);
        }
        Term term = Term.create(UUID.randomUUID(), command.canonicalName(),
                command.domain(), command.definition());
        term.changeStatus(command.status() == null ? TermStatus.DRAFT : command.status());
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
    public TermResponse updateTerm(UUID termId, CreateTermCommand command) {
        Term term = getTermOrThrow(termId);
        TermStatus status = command.status() == null ? term.getStatus() : command.status();
        term.update(command.canonicalName(), command.domain(), command.definition(), status);
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
    public SynonymResponse createSynonym(AddSynonymCommand command) {
        getTermOrThrow(command.termId());
        Synonym synonym = Synonym.create(UUID.randomUUID(), command.termId(),
                command.surface(), command.type());
        synonymRepository.save(synonym);
        return SynonymResponse.from(synonym);
    }

    public List<SynonymResponse> getSynonyms(UUID termId) {
        return synonymRepository.findAllByTermId(new TermId(termId)).stream().map(SynonymResponse::from).toList();
    }

    @Transactional
    public void deleteSynonym(UUID synonymId) {
        Synonym synonym = synonymRepository.findById(new SynonymId(synonymId))
                .orElseThrow(() -> new BusinessException(ErrorCode.SYNONYM_NOT_FOUND));
        synonym.softDelete();
    }

    // ===== Mapping =====

    @Transactional
    public SchemaMappingResponse createMapping(MapTermToColumnCommand command) {
        getTermOrThrow(command.termId());
        schemaCatalogRepository.findById(new SchemaCatalogId(command.schemaCatalogId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEMA_CATALOG_NOT_FOUND));
        if (schemaMappingRepository.existsByTermIdAndSchemaCatalogId(
                new TermId(command.termId()), new SchemaCatalogId(command.schemaCatalogId()))) {
            throw new BusinessException(ErrorCode.DUPLICATE_MAPPING);
        }
        SchemaMapping mapping = SchemaMapping.create(
                UUID.randomUUID(),
                command.termId(),
                command.schemaCatalogId(),
                command.mappingType(),
                command.codeValueRule());
        schemaMappingRepository.save(mapping);
        return SchemaMappingResponse.from(mapping);
    }

    public List<SchemaMappingResponse> getMappingsByTerm(UUID termId) {
        return schemaMappingRepository.findAllByTermId(new TermId(termId)).stream()
                .map(SchemaMappingResponse::from).toList();
    }

    @Transactional
    public void deleteMapping(UUID schemaMappingId) {
        SchemaMapping mapping = schemaMappingRepository.findById(new SchemaMappingId(schemaMappingId))
                .orElseThrow(() -> new BusinessException(ErrorCode.MAPPING_NOT_FOUND));
        mapping.softDelete();
    }

    private Term getTermOrThrow(UUID termId) {
        return termRepository.findById(new TermId(termId))
                .orElseThrow(() -> new BusinessException(ErrorCode.TERM_NOT_FOUND));
    }
}
