package com.hris.metadata.seed;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 시드 사전 CSV 정합성 게이트 (Spring 미기동 — 순수 JUnit).
 * <p>
 * 사전 데이터의 자기모순(존재하지 않는 용어/컬럼 참조, 코드값 불일치, 동의어 충돌)을
 * 데이터 작성 시점에 차단한다. DataSeeder 적재 형식과 동일한 규칙으로 파싱한다.
 * <ul>
 *   <li>다값 필드(코드값 동의어, 트리거 키워드)는 파일에서 {@code |} 구분 — 적재 시 콤마로 치환</li>
 *   <li>콤마는 컬럼 구분 전용 — 본문 텍스트에는 금지(가운뎃점 사용)</li>
 *   <li>canonicalName/surface 는 공백 금지(resolve 가 공백 토큰 단위 정확 일치이므로)</li>
 * </ul>
 */
class SeedDictionaryIntegrityTest {

    private static final String SEED_DIR = "seed/settlement/";
    private static final Set<String> SYNONYM_TYPES = Set.of("ABBREVIATION", "KOR_ENG", "TYPO", "COLLOQUIAL");
    private static final Set<String> DATA_TYPES = Set.of("varchar", "char", "numeric", "integer", "date", "timestamp");
    private static final Set<String> MAPPING_TYPES = Set.of("DIRECT", "CODE");
    private static final Set<String> OPERATORS = Set.of("EQ", "IN", "GTE", "LTE", "LIKE", "BETWEEN");

    private static List<String[]> terms;
    private static List<String[]> synonyms;
    private static List<String[]> catalog;
    private static List<String[]> codeValues;
    private static List<String[]> mappings;
    private static List<String[]> patterns;
    private static List<String[]> goldQueries;

    private static Set<String> canonicalNames;
    private static Set<String> catalogColumns;      // "table.column"
    private static Set<String> catalogColumnNames;  // "column"
    private static Map<String, String> dataTypeByColumn;        // "table.column" -> dataType
    private static Map<String, Set<String>> codesByColumn;      // "table.column" -> codes
    private static Map<String, Set<String>> codesByColumnName;  // "column" -> codes (테이블 무관)

    @BeforeAll
    static void load() {
        terms = readCsv(SEED_DIR + "terms.csv", 3);
        synonyms = readCsv(SEED_DIR + "synonyms.csv", 3);
        catalog = readCsv(SEED_DIR + "schema_catalog.csv", 5);
        codeValues = readCsv(SEED_DIR + "code_values.csv", 5);
        mappings = readCsv(SEED_DIR + "mappings.csv", 5);
        patterns = readCsv(SEED_DIR + "sql_patterns.csv", 5);
        goldQueries = readCsv("evaluation/gold_queries.csv", 5);

        canonicalNames = new HashSet<>();
        terms.forEach(row -> canonicalNames.add(row[0]));

        catalogColumns = new HashSet<>();
        catalogColumnNames = new HashSet<>();
        dataTypeByColumn = new HashMap<>();
        for (String[] row : catalog) {
            String key = row[0] + "." + row[1];
            catalogColumns.add(key);
            catalogColumnNames.add(row[1]);
            dataTypeByColumn.put(key, row[2]);
        }

        codesByColumn = new HashMap<>();
        codesByColumnName = new HashMap<>();
        for (String[] row : codeValues) {
            codesByColumn.computeIfAbsent(row[0] + "." + row[1], k -> new HashSet<>()).add(row[2]);
            codesByColumnName.computeIfAbsent(row[1], k -> new HashSet<>()).add(row[2]);
        }
    }

    // ── 불변식 1: 용어 ──────────────────────────────────────────────

    @Test
    @DisplayName("terms: canonicalName 전역 유일·공백 금지·domain·definition 필수")
    void termsAreValid() {
        Set<String> seen = new HashSet<>();
        for (String[] row : terms) {
            assertThat(row[0]).as("canonicalName 공백 금지: %s", row[0]).doesNotContainAnyWhitespaces();
            assertThat(seen.add(row[0])).as("canonicalName 중복: %s", row[0]).isTrue();
            assertThat(row[1]).as("domain 필수: %s", row[0]).isNotBlank();
            assertThat(row[2]).as("definition 필수: %s", row[0]).isNotBlank();
        }
    }

    // ── 불변식 2: 동의어 ────────────────────────────────────────────

    @Test
    @DisplayName("synonyms: 용어 실존·surface 전역 유일·canonicalName 과 불일치·공백 금지·타입 유효")
    void synonymsAreValid() {
        Set<String> seenSurfaces = new HashSet<>();
        for (String[] row : synonyms) {
            String term = row[0];
            String surface = row[1];
            assertThat(canonicalNames).as("동의어가 가리키는 용어 실존: %s → %s", surface, term).contains(term);
            assertThat(surface).as("surface 공백 금지: %s", surface).doesNotContainAnyWhitespaces();
            assertThat(seenSurfaces.add(surface))
                    .as("surface 전역 유일(중복 시 fetchFirst 비결정): %s", surface).isTrue();
            assertThat(canonicalNames)
                    .as("surface 가 어떤 canonicalName 과도 같으면 안 됨(토큰 해석 충돌): %s", surface)
                    .doesNotContain(surface);
            assertThat(SYNONYM_TYPES).as("synonymType 유효: %s", row[2]).contains(row[2]);
        }
    }

    // ── 불변식 3: 스키마 카탈로그 ───────────────────────────────────

    @Test
    @DisplayName("schema_catalog: (table,column) 유일·dataType 유효·설명·출처 필수")
    void catalogIsValid() {
        Set<String> seen = new HashSet<>();
        for (String[] row : catalog) {
            String key = row[0] + "." + row[1];
            assertThat(seen.add(key)).as("(table,column) 중복: %s", key).isTrue();
            assertThat(DATA_TYPES).as("dataType 유효: %s (%s)", row[2], key).contains(row[2]);
            assertThat(row[3]).as("description 필수: %s", key).isNotBlank();
            assertThat(row[4]).as("sourceSystem 필수: %s", key).isNotBlank();
        }
    }

    // ── 불변식 4: 코드값 ────────────────────────────────────────────

    @Test
    @DisplayName("code_values: 컬럼 실존·문자형 컬럼만·(컬럼,code) 유일·label 필수·synonym 비공백·컬럼 내 유일")
    void codeValuesAreValid() {
        Set<String> seen = new HashSet<>();
        Map<String, Set<String>> seenSynonymsByColumn = new HashMap<>();
        for (String[] row : codeValues) {
            String column = row[0] + "." + row[1];
            String code = row[2];
            assertThat(catalogColumns).as("코드값 컬럼 실존: %s", column).contains(column);
            assertThat(dataTypeByColumn.get(column))
                    .as("코드값은 문자형 컬럼에만: %s", column).isIn("varchar", "char");
            assertThat(seen.add(column + "=" + code)).as("(컬럼,code) 중복: %s=%s", column, code).isTrue();
            assertThat(row[3]).as("label 필수: %s=%s", column, code).isNotBlank();
            // 코드값 동의어: 비공백 + 같은 (table.column) 안에서 유일
            // (중복 시 resolve 의 codeByColumn 덮어쓰기가 비결정이 된다)
            if (!row[4].isBlank()) {
                Set<String> seenSynonyms = seenSynonymsByColumn.computeIfAbsent(column, k -> new HashSet<>());
                for (String synonym : row[4].split("\\|")) {
                    assertThat(synonym).as("코드값 synonym 공백 금지: %s=%s", column, code).isNotBlank();
                    assertThat(seenSynonyms.add(synonym))
                            .as("코드값 synonym 컬럼 내 중복(codeByColumn 덮어쓰기 비결정): %s '%s'", column, synonym)
                            .isTrue();
                }
            }
        }
    }

    // ── 불변식 5: 매핑 ──────────────────────────────────────────────

    @Test
    @DisplayName("mappings: 용어·컬럼 실존·(용어,컬럼) 유일·CODE 룰은 해당 컬럼의 실존 코드만")
    void mappingsAreValid() {
        Set<String> seen = new HashSet<>();
        for (String[] row : mappings) {
            String term = row[0];
            String column = row[1] + "." + row[2];
            String mappingType = row[3];
            String rule = row[4];
            assertThat(canonicalNames).as("매핑 용어 실존: %s", term).contains(term);
            assertThat(catalogColumns).as("매핑 컬럼 실존: %s → %s", term, column).contains(column);
            assertThat(seen.add(term + "→" + column)).as("(용어,컬럼) 매핑 중복: %s → %s", term, column).isTrue();
            assertThat(MAPPING_TYPES).as("mappingType 유효: %s", mappingType).contains(mappingType);
            if (mappingType.equals("CODE")) {
                assertThat(rule).as("CODE 매핑은 codeValueRule 필수: %s", term).isNotBlank();
                assertThat(codesByColumn.getOrDefault(column, Set.of()))
                        .as("codeValueRule 은 해당 컬럼의 실존 코드: %s → %s=%s", term, column, rule)
                        .contains(rule);
            } else {
                assertThat(rule).as("DIRECT 매핑은 codeValueRule 없음: %s", term).isBlank();
            }
        }
    }

    // ── 불변식 6: SQL 패턴 ──────────────────────────────────────────

    @Test
    @DisplayName("sql_patterns: 컬럼 실존·연산자 유효·EQ 리터럴 값은 해당 컬럼명의 실존 코드")
    void patternsAreValid() {
        for (String[] row : patterns) {
            String triggers = row[0];
            String column = row[1];
            String operator = row[2];
            String template = row[3];
            for (String trigger : triggers.split("\\|")) {
                assertThat(trigger).as("빈 트리거 금지: '%s'", triggers).isNotBlank();
            }
            assertThat(catalogColumnNames).as("패턴 대상 컬럼 실존: %s", column).contains(column);
            assertThat(OPERATORS).as("operator 유효: %s", operator).contains(operator);
            assertThat(Integer.parseInt(row[4])).as("priority ≥ 1").isGreaterThanOrEqualTo(1);
            if (operator.equals("EQ") && !template.startsWith(":")) {
                assertThat(codesByColumnName.getOrDefault(column, Set.of()))
                        .as("EQ 리터럴 값은 %s 컬럼의 실존 코드: %s", column, template)
                        .contains(template);
            }
        }
    }

    // ── 불변식 7: 평가 정답셋 상호참조 ──────────────────────────────

    @Test
    @DisplayName("gold_queries: queryId 유일·기대 매핑 컬럼/코드 실존")
    void goldQueriesAreValid() {
        Set<String> seenIds = new HashSet<>();
        for (String[] row : goldQueries) {
            assertThat(seenIds.add(row[0])).as("queryId 중복: %s", row[0]).isTrue();
            assertThat(row[1]).as("query 필수: %s", row[0]).isNotBlank();
            for (String expected : row[2].split("\\|")) {
                String columnPart = expected.contains("=") ? expected.substring(0, expected.indexOf('=')) : expected;
                assertThat(catalogColumns)
                        .as("기대 매핑 컬럼 실존: %s (%s)", expected, row[0]).contains(columnPart);
                if (expected.contains("=")) {
                    String code = expected.substring(expected.indexOf('=') + 1);
                    assertThat(codesByColumn.getOrDefault(columnPart, Set.of()))
                            .as("기대 코드 실존: %s (%s)", expected, row[0]).contains(code);
                }
            }
            assertThat(row[3]).as("expectsTimeRange 는 true/false: %s", row[0]).isIn("true", "false");
        }
    }

    // ── 불변식 8: 운영 사전 규모 하한 게이트 ───────────────────────

    @Test
    @DisplayName("규모 게이트: 용어 ≥180 · 매핑 ≥150 · 동의어 ≥200 · 카탈로그 ≥100 · 코드값 ≥60 · 패턴 ≥25")
    void scaleGates() {
        assertThat(terms.size()).as("terms").isGreaterThanOrEqualTo(180);
        assertThat(mappings.size()).as("mappings").isGreaterThanOrEqualTo(150);
        assertThat(synonyms.size()).as("synonyms").isGreaterThanOrEqualTo(200);
        assertThat(catalog.size()).as("catalog").isGreaterThanOrEqualTo(100);
        assertThat(codeValues.size()).as("codeValues").isGreaterThanOrEqualTo(60);
        assertThat(patterns.size()).as("patterns").isGreaterThanOrEqualTo(25);
        assertThat(goldQueries.size()).as("goldQueries").isGreaterThanOrEqualTo(60);
    }

    // ── 불변식 9: 기존 통합 테스트가 의존하는 시드 보존 ────────────

    @Test
    @DisplayName("기존 시드 보존: 미정산→정산상태 동의어 · settlement_status=PENDING · 핵심 6용어")
    void legacySeedPreserved() {
        assertThat(canonicalNames).contains("정산금액", "정산상태", "수수료", "지급일", "가맹점", "정산주기");
        assertThat(synonyms).anySatisfy(row -> {
            assertThat(row[1]).isEqualTo("미정산");
            assertThat(row[0]).isEqualTo("정산상태");
        });
        assertThat(codesByColumn.getOrDefault("settlement.settlement_status", Set.of()))
                .contains("PENDING", "SETTLED", "HOLD", "CANCELED");
        assertThat(mappings).anySatisfy(row -> {
            assertThat(row[0]).isEqualTo("정산상태");
            assertThat(row[1]).isEqualTo("settlement");
            assertThat(row[2]).isEqualTo("settlement_status");
        });
    }

    // ── CSV 파싱 (DataSeeder 와 동일 규칙) ─────────────────────────

    private static List<String[]> readCsv(String path, int expectedColumns) {
        InputStream in = SeedDictionaryIntegrityTest.class.getClassLoader().getResourceAsStream(path);
        assertThat(in).as("리소스 존재: %s", path).isNotNull();
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
                assertThat(cells.length)
                        .as("%s:%d 컬럼 수 %d 이어야 함(본문 콤마 금지): %s", path, lineNo, expectedColumns, line)
                        .isEqualTo(expectedColumns);
                for (int i = 0; i < cells.length; i++) {
                    cells[i] = cells[i].trim();
                }
                rows.add(cells);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return rows;
    }
}
