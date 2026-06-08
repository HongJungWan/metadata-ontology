package com.hris.metadata.presentation.term;

import com.hris.metadata.application.term.DictionaryImportService;
import com.hris.metadata.application.term.ImportResult;
import com.hris.metadata.application.term.TermAdminService;
import com.hris.metadata.application.term.dto.request.SchemaMappingRequest;
import com.hris.metadata.application.term.dto.request.SynonymRequest;
import com.hris.metadata.application.term.dto.request.TermRequest;
import com.hris.metadata.application.term.dto.response.SchemaMappingResponse;
import com.hris.metadata.application.term.dto.response.SynonymResponse;
import com.hris.metadata.application.term.dto.response.TermResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 사전 관리 API 컨트롤러 (PRD §5).
 * <p>
 * 용어/동의어/매핑 CRUD 와 CSV 일괄 임포트, 상태 승인을 제공한다.
 */
@Tag(name = "Term Admin", description = "사전(용어/동의어/매핑) 관리 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class TermAdminController {

    private final TermAdminService termAdminService;
    private final DictionaryImportService dictionaryImportService;

    // ===== Term =====

    @Operation(summary = "표준 용어 생성")
    @PostMapping("/terms")
    public ResponseEntity<TermResponse> createTerm(@Valid @RequestBody TermRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(termAdminService.createTerm(request));
    }

    @Operation(summary = "표준 용어 목록 조회")
    @GetMapping("/terms")
    public ResponseEntity<List<TermResponse>> getTerms() {
        return ResponseEntity.ok(termAdminService.getTerms());
    }

    @Operation(summary = "표준 용어 상세 조회")
    @GetMapping("/terms/{termId}")
    public ResponseEntity<TermResponse> getTerm(@PathVariable UUID termId) {
        return ResponseEntity.ok(termAdminService.getTerm(termId));
    }

    @Operation(summary = "표준 용어 수정")
    @PutMapping("/terms/{termId}")
    public ResponseEntity<TermResponse> updateTerm(@PathVariable UUID termId,
                                                   @Valid @RequestBody TermRequest request) {
        return ResponseEntity.ok(termAdminService.updateTerm(termId, request));
    }

    @Operation(summary = "표준 용어 승인 (검토 후 ACTIVE 활성화)")
    @PostMapping("/terms/{termId}/approve")
    public ResponseEntity<TermResponse> approveTerm(@PathVariable UUID termId) {
        return ResponseEntity.ok(termAdminService.approveTerm(termId));
    }

    @Operation(summary = "표준 용어 삭제 (소프트 삭제)")
    @DeleteMapping("/terms/{termId}")
    public ResponseEntity<Void> deleteTerm(@PathVariable UUID termId) {
        termAdminService.deleteTerm(termId);
        return ResponseEntity.noContent().build();
    }

    // ===== Synonym =====

    @Operation(summary = "동의어 생성")
    @PostMapping("/synonyms")
    public ResponseEntity<SynonymResponse> createSynonym(@Valid @RequestBody SynonymRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(termAdminService.createSynonym(request));
    }

    @Operation(summary = "용어별 동의어 목록 조회")
    @GetMapping("/terms/{termId}/synonyms")
    public ResponseEntity<List<SynonymResponse>> getSynonyms(@PathVariable UUID termId) {
        return ResponseEntity.ok(termAdminService.getSynonyms(termId));
    }

    @Operation(summary = "동의어 삭제 (소프트 삭제)")
    @DeleteMapping("/synonyms/{synonymId}")
    public ResponseEntity<Void> deleteSynonym(@PathVariable UUID synonymId) {
        termAdminService.deleteSynonym(synonymId);
        return ResponseEntity.noContent().build();
    }

    // ===== Mapping =====

    @Operation(summary = "용어-스키마 매핑 생성")
    @PostMapping("/mappings")
    public ResponseEntity<SchemaMappingResponse> createMapping(
            @Valid @RequestBody SchemaMappingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(termAdminService.createMapping(request));
    }

    @Operation(summary = "용어별 매핑 목록 조회")
    @GetMapping("/terms/{termId}/mappings")
    public ResponseEntity<List<SchemaMappingResponse>> getMappings(@PathVariable UUID termId) {
        return ResponseEntity.ok(termAdminService.getMappingsByTerm(termId));
    }

    @Operation(summary = "매핑 삭제 (소프트 삭제)")
    @DeleteMapping("/mappings/{schemaMappingId}")
    public ResponseEntity<Void> deleteMapping(@PathVariable UUID schemaMappingId) {
        termAdminService.deleteMapping(schemaMappingId);
        return ResponseEntity.noContent().build();
    }

    // ===== Import =====

    @Operation(summary = "사전 CSV 일괄 임포트",
            description = "CSV 형식: canonicalName,domain,definition,synonym,synonymType")
    @PostMapping(value = "/import", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<ImportResult> importCsv(@RequestBody String csv) {
        return ResponseEntity.ok(dictionaryImportService.importCsv(csv));
    }
}
