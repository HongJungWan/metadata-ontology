package com.hris.metadata.application.promptcontext;

import com.hris.metadata.application.expand.ExpansionResult;
import com.hris.metadata.application.expand.ExpansionService;
import com.hris.metadata.domain.mapping.ColumnMapping;
import com.hris.metadata.domain.mapping.SchemaMappingRepository;
import com.hris.metadata.domain.schema.CodeValue;
import com.hris.metadata.domain.schema.CodeValueRepository;
import com.hris.metadata.domain.schema.SchemaCatalog;
import com.hris.metadata.domain.schema.SchemaCatalogRepository;
import com.hris.metadata.domain.term.Synonym;
import com.hris.metadata.domain.term.SynonymRepository;
import com.hris.metadata.domain.term.Term;
import com.hris.metadata.domain.term.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * LLM 스키마 설명 블록 생성 서비스 (응용 서비스).
 * <p>
 * 질의에 걸리는 테이블·컬럼·코드값·표준 용어 설명을 모아, PRD §6 형식의 안내문을 만든다.
 * 이 안내문을 LLM 컨텍스트에 먼저 주면 컬럼 의미를 임의로 지어내는 것을 막는다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromptContextService {

    private final ExpansionService expansionService;
    private final TermRepository termRepository;
    private final SynonymRepository synonymRepository;
    private final SchemaMappingRepository schemaMappingRepository;
    private final SchemaCatalogRepository schemaCatalogRepository;
    private final CodeValueRepository codeValueRepository;

    /**
     * 질의에서 표준 용어를 추출해 스키마 설명 블록을 만든다.
     */
    public String buildFromQuery(String query) {
        ExpansionResult expanded = expansionService.expand(query);
        List<Term> terms = resolveTerms(expanded.getExpandedQuery());
        return build(terms);
    }

    /**
     * 표준 용어명 목록으로부터 직접 스키마 설명 블록을 만든다.
     */
    public String buildFromTerms(List<String> termNames) {
        if (termNames == null || termNames.isEmpty()) {
            return build(List.of());
        }
        List<Term> terms = new ArrayList<>();
        for (String name : termNames) {
            termRepository.findByCanonicalName(name).ifPresent(terms::add);
        }
        return build(terms);
    }

    private List<Term> resolveTerms(String expandedQuery) {
        List<Term> terms = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (String token : expandedQuery.trim().split("\\s+")) {
            if (token.isBlank() || !seen.add(token)) {
                continue;
            }
            termRepository.findByCanonicalName(token).ifPresent(terms::add);
        }
        return terms;
    }

    private String build(List<Term> terms) {
        List<UUID> termIds = terms.stream().map(Term::getTermId).toList();

        StringBuilder builder = new StringBuilder();
        builder.append("[검색 가능한 스키마]\n");
        appendSchemaSection(builder, termIds);
        builder.append("[표준 용어]\n");
        appendTermSection(builder, terms);
        return builder.toString().stripTrailing();
    }

    private void appendSchemaSection(StringBuilder builder, List<UUID> termIds) {
        List<ColumnMapping> rows = schemaMappingRepository.findColumnMappingsByTermIds(termIds);

        Set<String> renderedTables = new LinkedHashSet<>();
        for (ColumnMapping row : rows) {
            if (renderedTables.add(row.physicalTable())) {
                builder.append("테이블: ").append(row.physicalTable()).append("\n");
            }
            appendColumnLine(builder, row);
        }
    }

    private void appendColumnLine(StringBuilder builder, ColumnMapping row) {
        Optional<SchemaCatalog> catalog = schemaCatalogRepository
                .findByPhysicalTableAndPhysicalColumn(row.physicalTable(), row.physicalColumn());
        builder.append("- ").append(row.physicalColumn());
        catalog.map(SchemaCatalog::getDescription)
                .filter(description -> description != null && !description.isBlank())
                .ifPresent(description -> builder.append(": ").append(description));
        catalog.ifPresent(value -> appendCodeValues(builder, value));
        builder.append("\n");
    }

    private void appendCodeValues(StringBuilder builder, SchemaCatalog catalog) {
        List<CodeValue> codeValues = codeValueRepository.findAllBySchemaCatalogId(catalog.getSchemaCatalogId());
        if (codeValues.isEmpty()) {
            return;
        }
        String joined = codeValues.stream()
                .map(code -> code.getCode() + "=" + (code.getLabel() == null ? code.getCode() : code.getLabel()))
                .collect(Collectors.joining(", "));
        builder.append(" (").append(joined).append(")");
    }

    private void appendTermSection(StringBuilder builder, List<Term> terms) {
        for (Term term : terms) {
            builder.append(term.getCanonicalName());
            if (term.getDefinition() != null && !term.getDefinition().isBlank()) {
                builder.append(" = ").append(term.getDefinition());
            }
            appendSynonyms(builder, term);
            builder.append("\n");
        }
    }

    private void appendSynonyms(StringBuilder builder, Term term) {
        List<Synonym> synonyms = synonymRepository.findAllByTermId(term.getTermId());
        if (synonyms.isEmpty()) {
            return;
        }
        String joined = synonyms.stream().map(Synonym::getSurface).collect(Collectors.joining(", "));
        builder.append(" (동의어: ").append(joined).append(")");
    }
}
