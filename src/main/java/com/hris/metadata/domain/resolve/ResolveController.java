package com.hris.metadata.domain.resolve;

import com.hris.metadata.domain.expand.ExpansionResult;
import com.hris.metadata.domain.expand.ExpansionService;
import com.hris.metadata.domain.normalize.NormalizationResult;
import com.hris.metadata.domain.normalize.NormalizationService;
import com.hris.metadata.domain.pattern.SqlPatternMatch;
import com.hris.metadata.domain.pattern.SqlPatternService;
import com.hris.metadata.domain.promptcontext.PromptContextService;
import com.hris.metadata.domain.resolve.dto.request.MatchSqlPatternRequest;
import com.hris.metadata.domain.resolve.dto.request.PromptContextRequest;
import com.hris.metadata.domain.resolve.dto.request.ResolveRequest;
import com.hris.metadata.domain.resolve.dto.response.ResolveResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ResolveResponse> resolve(@Valid @RequestBody ResolveRequest request) {
        return ResponseEntity.ok(resolveService.resolve(request.getQuery()));
    }

    @Operation(summary = "동의어 확장", description = "질의의 동의어를 표준 용어로 펼친다.")
    @PostMapping("/expand")
    public ResponseEntity<ExpansionResult> expand(@Valid @RequestBody ResolveRequest request) {
        return ResponseEntity.ok(expansionService.expand(request.getQuery()));
    }

    @Operation(summary = "기간 정규화", description = "\"지난달\" 등 상대 기간 표현을 실제 날짜 범위로 변환한다.")
    @PostMapping("/normalize")
    public ResponseEntity<NormalizationResult> normalize(@Valid @RequestBody ResolveRequest request) {
        return ResponseEntity.ok(normalizationService.normalize(request.getQuery()));
    }

    @Operation(summary = "SQL 패턴 매칭", description = "키워드를 컬럼·연산자·값 후보로 매핑한다.")
    @PostMapping("/match-sql-pattern")
    public ResponseEntity<List<SqlPatternMatch>> matchSqlPattern(
            @Valid @RequestBody MatchSqlPatternRequest request) {
        return ResponseEntity.ok(sqlPatternService.match(request.getKeywords()));
    }

    @Operation(summary = "프롬프트 컨텍스트 생성", description = "LLM 에 줄 스키마 설명 블록을 만든다.")
    @PostMapping("/prompt-context")
    public ResponseEntity<Map<String, String>> promptContext(@RequestBody PromptContextRequest request) {
        String context = (request.getTerms() != null && !request.getTerms().isEmpty())
                ? promptContextService.buildFromTerms(request.getTerms())
                : promptContextService.buildFromQuery(request.getQuery());
        return ResponseEntity.ok(Map.of("promptContext", context));
    }
}
