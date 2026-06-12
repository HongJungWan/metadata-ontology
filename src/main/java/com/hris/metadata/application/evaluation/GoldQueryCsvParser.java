package com.hris.metadata.application.evaluation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 정답셋 CSV 파서 (순수 유틸).
 * <p>
 * 형식: {@code queryId,query,expectedMappings,expectsTimeRange,note} — 첫 줄은 헤더.
 * expectedMappings 는 {@code table.column[=CODE]} 를 {@code |} 로 연결한다.
 */
public final class GoldQueryCsvParser {

    private static final int COLUMNS = 5;

    private GoldQueryCsvParser() {
    }

    public static List<GoldQuery> parse(InputStream in) {
        List<GoldQuery> queries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // 헤더 스킵
            int lineNo = 1;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) {
                    continue;
                }
                String[] cells = line.split(",", -1);
                if (cells.length != COLUMNS) {
                    throw new IllegalStateException(
                            "gold_queries.csv:" + lineNo + " 컬럼 수가 " + COLUMNS + " 가 아님: " + line);
                }
                List<GoldQuery.ExpectedMapping> expected = Arrays.stream(cells[2].trim().split("\\|"))
                        .map(String::trim)
                        .map(GoldQuery.ExpectedMapping::parse)
                        .toList();
                queries.add(new GoldQuery(
                        cells[0].trim(), cells[1].trim(), expected, Boolean.parseBoolean(cells[3].trim())));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("gold_queries.csv 읽기 실패", e);
        }
        return queries;
    }
}
