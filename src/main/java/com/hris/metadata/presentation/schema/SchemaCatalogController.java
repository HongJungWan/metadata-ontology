package com.hris.metadata.presentation.schema;

import com.hris.metadata.application.schema.SchemaCatalogService;
import com.hris.metadata.presentation.schema.dto.request.CodeValueRequest;
import com.hris.metadata.presentation.schema.dto.request.SchemaCatalogRequest;
import com.hris.metadata.presentation.schema.dto.response.CodeValueResponse;
import com.hris.metadata.presentation.schema.dto.response.SchemaCatalogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
 * 물리 스키마 카탈로그·코드값 관리 API.
 */
@Tag(name = "Schema Catalog", description = "물리 스키마 카탈로그/코드값 관리 API")
@RestController
@RequestMapping("/api/admin/schema")
@RequiredArgsConstructor
public class SchemaCatalogController {

    private final SchemaCatalogService schemaCatalogService;

    @Operation(summary = "스키마 카탈로그 생성")
    @PostMapping("/catalogs")
    public ResponseEntity<SchemaCatalogResponse> createCatalog(
            @Valid @RequestBody SchemaCatalogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(schemaCatalogService.createCatalog(request));
    }

    @Operation(summary = "스키마 카탈로그 목록 조회")
    @GetMapping("/catalogs")
    public ResponseEntity<List<SchemaCatalogResponse>> getCatalogs() {
        return ResponseEntity.ok(schemaCatalogService.getCatalogs());
    }

    @Operation(summary = "스키마 카탈로그 수정")
    @PutMapping("/catalogs/{schemaCatalogId}")
    public ResponseEntity<SchemaCatalogResponse> updateCatalog(
            @PathVariable UUID schemaCatalogId, @Valid @RequestBody SchemaCatalogRequest request) {
        return ResponseEntity.ok(schemaCatalogService.updateCatalog(schemaCatalogId, request));
    }

    @Operation(summary = "스키마 카탈로그 삭제 (소프트 삭제)")
    @DeleteMapping("/catalogs/{schemaCatalogId}")
    public ResponseEntity<Void> deleteCatalog(@PathVariable UUID schemaCatalogId) {
        schemaCatalogService.deleteCatalog(schemaCatalogId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "코드값 생성")
    @PostMapping("/code-values")
    public ResponseEntity<CodeValueResponse> createCodeValue(@Valid @RequestBody CodeValueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(schemaCatalogService.createCodeValue(request));
    }

    @Operation(summary = "카탈로그별 코드값 목록 조회")
    @GetMapping("/catalogs/{schemaCatalogId}/code-values")
    public ResponseEntity<List<CodeValueResponse>> getCodeValues(@PathVariable UUID schemaCatalogId) {
        return ResponseEntity.ok(schemaCatalogService.getCodeValues(schemaCatalogId));
    }

    @Operation(summary = "코드값 삭제 (소프트 삭제)")
    @DeleteMapping("/code-values/{codeValueId}")
    public ResponseEntity<Void> deleteCodeValue(@PathVariable UUID codeValueId) {
        schemaCatalogService.deleteCodeValue(codeValueId);
        return ResponseEntity.noContent().build();
    }
}
