package com.hris.metadata.presentation.pattern;

import com.hris.metadata.application.pattern.SqlPatternAdminService;
import com.hris.metadata.application.pattern.command.DefineSqlPatternCommand;
import com.hris.metadata.application.pattern.dto.response.SqlPatternResponse;
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
 * SQL 패턴 관리 API.
 */
@Tag(name = "SQL Pattern Admin", description = "SQL 패턴 규칙 관리 API")
@RestController
@RequestMapping("/api/admin/sql-patterns")
@RequiredArgsConstructor
public class SqlPatternAdminController {

    private final SqlPatternAdminService sqlPatternAdminService;

    @Operation(summary = "SQL 패턴 생성")
    @PostMapping
    public ResponseEntity<SqlPatternResponse> create(@Valid @RequestBody DefineSqlPatternCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sqlPatternAdminService.create(command));
    }

    @Operation(summary = "SQL 패턴 목록 조회")
    @GetMapping
    public ResponseEntity<List<SqlPatternResponse>> getAll() {
        return ResponseEntity.ok(sqlPatternAdminService.getAll());
    }

    @Operation(summary = "SQL 패턴 수정")
    @PutMapping("/{sqlPatternId}")
    public ResponseEntity<SqlPatternResponse> update(@PathVariable UUID sqlPatternId,
                                                     @Valid @RequestBody DefineSqlPatternCommand command) {
        return ResponseEntity.ok(sqlPatternAdminService.update(sqlPatternId, command));
    }

    @Operation(summary = "SQL 패턴 삭제 (소프트 삭제)")
    @DeleteMapping("/{sqlPatternId}")
    public ResponseEntity<Void> delete(@PathVariable UUID sqlPatternId) {
        sqlPatternAdminService.delete(sqlPatternId);
        return ResponseEntity.noContent().build();
    }
}
