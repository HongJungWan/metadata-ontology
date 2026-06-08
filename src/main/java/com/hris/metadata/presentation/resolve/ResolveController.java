package com.hris.metadata.presentation.resolve;

import com.hris.metadata.domain.expand.ExpansionResult;
import com.hris.metadata.domain.expand.ExpansionService;
import com.hris.metadata.domain.normalize.NormalizationResult;
import com.hris.metadata.domain.normalize.NormalizationService;
import com.hris.metadata.domain.pattern.SqlPatternMatch;
import com.hris.metadata.domain.pattern.SqlPatternService;
import com.hris.metadata.application.promptcontext.PromptContextService;
import com.hris.metadata.application.resolve.ResolveService;
import com.hris.metadata.application.resolve.command.BuildPromptContextCommand;
import com.hris.metadata.application.resolve.command.MatchSqlPatternCommand;
import com.hris.metadata.application.resolve.command.ResolveQueryCommand;
import com.hris.metadata.application.resolve.dto.response.ResolveResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 매핑·확장 API 컨트롤러 (PRD §4).
 * <p>
 * P1(knowledge-search) 이 자연어 질의를 물리 컬럼·코드값·기간으로 해석하기 위해 호출한다.
 */
@Tag(name = "Resolve", description = "질의 해석/확장/정규화/패턴 매칭 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ResolveController {

    private final ResolveService resolveService;
    private final ExpansionService expansionService;
    private final NormalizationService normalizationService;
    private final SqlPatternService sqlPatternService;
    private final PromptContextService promptContextService;

    @Operation(summary = "질의 해석", description = "정규화+동의어확장+컬럼/코드값 매핑을 한 번에 수행한다.")
    @PostMapping("/resolve")
    public ResponseEntity<ResolveResponse> resolve(@Valid @RequestBody ResolveQueryCommand command) {
        return ResponseEntity.ok(resolveService.resolve(command.query()));
    }

    @Operation(summary = "동의어 확장", description = "질의의 동의어를 표준 용어로 펼친다.")
    @PostMapping("/expand")
    public ResponseEntity<ExpansionResult> expand(@Valid @RequestBody ResolveQueryCommand command) {
        return ResponseEntity.ok(expansionService.expand(command.query()));
    }

    @Operation(summary = "기간 정규화", description = "\"지난달\" 등 상대 기간 표현을 실제 날짜 범위로 변환한다.")
    @PostMapping("/normalize")
    public ResponseEntity<NormalizationResult> normalize(@Valid @RequestBody ResolveQueryCommand command) {
        return ResponseEntity.ok(normalizationService.normalize(command.query(), LocalDate.now()));
    }

    @Operation(summary = "SQL 패턴 매칭", description = "키워드를 컬럼·연산자·값 후보로 매핑한다.")
    @PostMapping("/match-sql-pattern")
    public ResponseEntity<List<SqlPatternMatch>> matchSqlPattern(
            @Valid @RequestBody MatchSqlPatternCommand command) {
        return ResponseEntity.ok(sqlPatternService.match(command.keywords()));
    }

    @Operation(summary = "프롬프트 컨텍스트 생성", description = "LLM 에 줄 스키마 설명 블록을 만든다.")
    @PostMapping("/prompt-context")
    public ResponseEntity<Map<String, String>> promptContext(@RequestBody BuildPromptContextCommand command) {
        String context = (command.terms() != null && !command.terms().isEmpty())
                ? promptContextService.buildFromTerms(command.terms())
                : promptContextService.buildFromQuery(command.query());
        return ResponseEntity.ok(Map.of("promptContext", context));
    }
}
