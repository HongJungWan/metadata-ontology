package com.hris.metadata.infrastructure.config;

import com.hris.metadata.domain.mapping.SchemaMapping;
import com.hris.metadata.domain.mapping.SchemaMappingRepository;
import com.hris.metadata.domain.pattern.PatternOperator;
import com.hris.metadata.domain.pattern.SqlPattern;
import com.hris.metadata.domain.pattern.SqlPatternRepository;
import com.hris.metadata.domain.schema.CodeValue;
import com.hris.metadata.domain.schema.CodeValueRepository;
import com.hris.metadata.domain.schema.SchemaCatalog;
import com.hris.metadata.domain.schema.SchemaCatalogRepository;
import com.hris.metadata.domain.term.Synonym;
import com.hris.metadata.domain.term.SynonymRepository;
import com.hris.metadata.domain.term.SynonymType;
import com.hris.metadata.domain.term.Term;
import com.hris.metadata.domain.term.TermRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 정산(Settlement) 도메인 사전 시드 (infrastructure 설정).
 * <p>
 * H2 in-memory(local 프로필) 부팅 시 {@code seed/settlement/*.csv} 사전을 적재한다.
 * 파일 규칙: 첫 줄 헤더 · 콤마는 컬럼 구분 전용 · 다값 필드(코드값 동의어, 트리거 키워드)는
 * {@code |} 구분(적재 시 콤마로 치환해 기존 엔티티의 콤마-조인 형식 유지).
 * 시드 무결성은 {@code SeedDictionaryIntegrityTest} 가 같은 파싱 규칙으로 강제하며,
 * 여기서는 참조 해석 실패 시 fail-fast(부팅 실패)로 오염 적재를 차단한다.
 * 운영 프로필에서는 비활성(@Profile("local")) 이며, 이미 데이터가 있으면 건너뛴다.
 */
@Slf4j
@Order(1)
@Component
@Profile("local")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final String SEED_DIR = "seed/settlement/";

    private final TermRepository termRepository;
    private final SynonymRepository synonymRepository;
    private final SchemaCatalogRepository schemaCatalogRepository;
    private final CodeValueRepository codeValueRepository;
    private final SchemaMappingRepository schemaMappingRepository;
    private final SqlPatternRepository sqlPatternRepository;

    @Override
    public void run(String... args) {
        if (termRepository.count() > 0) {
            log.info("Settlement dictionary already seeded — skipping.");
            return;
        }
        log.info("Seeding settlement dictionary from {}*.csv (H2 local profile)...", SEED_DIR);

        Map<String, UUID> termIds = seedTerms();
        int synonymCount = seedSynonyms(termIds);
        Map<String, UUID> catalogIds = seedCatalog();
        int codeValueCount = seedCodeValues(catalogIds);
        int mappingCount = seedMappings(termIds, catalogIds);
        int patternCount = seedSqlPatterns();

        log.info("Settlement dictionary seeded: {} terms, {} synonyms, {} catalog columns, "
                        + "{} code values, {} mappings, {} sql patterns.",
                termIds.size(), synonymCount, catalogIds.size(), codeValueCount, mappingCount, patternCount);
    }

    private Map<String, UUID> seedTerms() {
        Map<String, UUID> ids = new HashMap<>();
        for (String[] row : readCsv("terms.csv", 3)) {
            UUID id = UUID.randomUUID();
            termRepository.save(Term.create(id, row[0], row[1], row[2]));
            ids.put(row[0], id);
        }
        return ids;
    }

    private int seedSynonyms(Map<String, UUID> termIds) {
        int count = 0;
        for (String[] row : readCsv("synonyms.csv", 3)) {
            synonymRepository.save(Synonym.create(
                    UUID.randomUUID(), requireTerm(termIds, row[0], "synonyms.csv"),
                    row[1], SynonymType.valueOf(row[2])));
            count++;
        }
        return count;
    }

    private Map<String, UUID> seedCatalog() {
        Map<String, UUID> ids = new HashMap<>();
        for (String[] row : readCsv("schema_catalog.csv", 5)) {
            UUID id = UUID.randomUUID();
            schemaCatalogRepository.save(SchemaCatalog.create(id, row[0], row[1], row[2], row[3], row[4]));
            ids.put(columnKey(row[0], row[1]), id);
        }
        return ids;
    }

    private int seedCodeValues(Map<String, UUID> catalogIds) {
        int count = 0;
        for (String[] row : readCsv("code_values.csv", 5)) {
            String synonyms = row[4].isBlank() ? null : row[4].replace('|', ',');
            codeValueRepository.save(CodeValue.create(
                    UUID.randomUUID(), requireCatalog(catalogIds, row[0], row[1], "code_values.csv"),
                    row[2], row[3], synonyms));
            count++;
        }
        return count;
    }

    private int seedMappings(Map<String, UUID> termIds, Map<String, UUID> catalogIds) {
        int count = 0;
        for (String[] row : readCsv("mappings.csv", 5)) {
            String codeValueRule = row[4].isBlank() ? null : row[4];
            schemaMappingRepository.save(SchemaMapping.create(
                    UUID.randomUUID(), requireTerm(termIds, row[0], "mappings.csv"),
                    requireCatalog(catalogIds, row[1], row[2], "mappings.csv"), row[3], codeValueRule));
            count++;
        }
        return count;
    }

    private int seedSqlPatterns() {
        int count = 0;
        for (String[] row : readCsv("sql_patterns.csv", 5)) {
            sqlPatternRepository.save(SqlPattern.create(
                    UUID.randomUUID(), row[0].replace('|', ','), row[1],
                    PatternOperator.valueOf(row[2]), row[3], Integer.parseInt(row[4])));
            count++;
        }
        return count;
    }

    private UUID requireTerm(Map<String, UUID> termIds, String canonicalName, String file) {
        UUID id = termIds.get(canonicalName);
        if (id == null) {
            throw new IllegalStateException(file + " 이 존재하지 않는 용어를 참조: " + canonicalName);
        }
        return id;
    }

    private UUID requireCatalog(Map<String, UUID> catalogIds, String table, String column, String file) {
        UUID id = catalogIds.get(columnKey(table, column));
        if (id == null) {
            throw new IllegalStateException(file + " 이 존재하지 않는 컬럼을 참조: " + columnKey(table, column));
        }
        return id;
    }

    private String columnKey(String table, String column) {
        return table + "." + column;
    }

    private List<String[]> readCsv(String fileName, int expectedColumns) {
        String path = SEED_DIR + fileName;
        InputStream in = getClass().getClassLoader().getResourceAsStream(path);
        if (in == null) {
            throw new IllegalStateException("시드 리소스를 찾을 수 없음: " + path);
        }
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // 헤더 스킵
            int lineNo = 1;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) {
                    continue;
                }
                String[] cells = line.split(",", -1);
                if (cells.length != expectedColumns) {
                    throw new IllegalStateException(
                            path + ":" + lineNo + " 컬럼 수가 " + expectedColumns + " 가 아님: " + line);
                }
                for (int i = 0; i < cells.length; i++) {
                    cells[i] = cells[i].trim();
                }
                rows.add(cells);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("시드 CSV 읽기 실패: " + path, e);
        }
        return rows;
    }
}
