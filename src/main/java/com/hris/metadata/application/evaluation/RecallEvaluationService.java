package com.hris.metadata.application.evaluation;

import com.hris.metadata.application.evaluation.dto.response.EvaluationReportResponse;
import com.hris.metadata.application.evaluation.dto.response.EvaluationReportResponse.ArmResult;
import com.hris.metadata.application.evaluation.dto.response.EvaluationReportResponse.QueryComparison;
import com.hris.metadata.application.resolve.ResolveOptions;
import com.hris.metadata.application.resolve.ResolveService;
import com.hris.metadata.application.resolve.dto.response.ResolveResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 재현율 평가 응용 서비스 (PRD §8).
 * <p>
 * 정답셋({@code evaluation/gold_queries.csv})의 각 질의를 동일한 {@link ResolveService} 경로로
 * 두 번 해석한다 — BASELINE({@link ResolveOptions#rawBaseline()}: 원문 토큰 그대로)과
 * FULL({@link ResolveOptions#full()}: 동의어 확장 + 기간 정규화). 두 결과의 재현율 차이가
 * 동의어 사전·정규화 계층의 기여분이다. 지표 정의는 {@link EvaluationReportResponse} 참조.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecallEvaluationService {

    private static final String GOLD_QUERIES_RESOURCE = "evaluation/gold_queries.csv";

    private final ResolveService resolveService;

    /**
     * 기본 정답셋({@code evaluation/gold_queries.csv})을 평가한다.
     *
     * @param referenceDate 기간 정규화 기준일 (호출자가 주입 — 결과 결정론 보장)
     */
    public EvaluationReportResponse evaluate(LocalDate referenceDate) {
        return evaluate(referenceDate, GOLD_QUERIES_RESOURCE);
    }

    /**
     * 지정 정답셋 리소스를 평가한다 (BASELINE / FULL / FUZZY 3-arm).
     * <p>
     * FUZZY = FULL + 퍼지 폴백. 메인 셋에서는 정확 매칭이 이미 커버해 FUZZY≈FULL(무회귀)이고,
     * 오타/OOV 셋({@code evaluation/gold_oov.csv})에서는 FUZZY 가 정확 미스를 회복해 FULL 을 능가한다.
     */
    public EvaluationReportResponse evaluate(LocalDate referenceDate, String goldResource) {
        List<GoldQuery> goldQueries = loadGoldQueries(goldResource);

        Map<String, QueryOutcome> baselineOutcomes = evaluateArm(goldQueries, referenceDate,
                ResolveOptions.rawBaseline());
        Map<String, QueryOutcome> fullOutcomes = evaluateArm(goldQueries, referenceDate,
                ResolveOptions.full());
        Map<String, QueryOutcome> fuzzyOutcomes = evaluateArm(goldQueries, referenceDate,
                ResolveOptions.fullWithFuzzy());

        List<QueryComparison> comparisons = new ArrayList<>();
        for (GoldQuery gold : goldQueries) {
            QueryOutcome baseline = baselineOutcomes.get(gold.queryId());
            QueryOutcome full = fullOutcomes.get(gold.queryId());
            comparisons.add(new QueryComparison(gold.queryId(), gold.query(),
                    baseline.recall(), full.recall(), baseline.missing(), full.missing()));
        }

        return new EvaluationReportResponse(
                goldQueries.size(),
                aggregate("BASELINE", goldQueries, baselineOutcomes),
                aggregate("FULL", goldQueries, fullOutcomes),
                aggregate("FUZZY", goldQueries, fuzzyOutcomes),
                comparisons);
    }

    private List<GoldQuery> loadGoldQueries(String resource) {
        InputStream in = getClass().getClassLoader().getResourceAsStream(resource);
        if (in == null) {
            throw new IllegalStateException("정답셋 리소스를 찾을 수 없음: " + resource);
        }
        return GoldQueryCsvParser.parse(in);
    }

    private Map<String, QueryOutcome> evaluateArm(List<GoldQuery> goldQueries, LocalDate referenceDate,
                                                  ResolveOptions options) {
        Map<String, QueryOutcome> outcomes = new LinkedHashMap<>();
        for (GoldQuery gold : goldQueries) {
            ResolveResponse response = resolveService.resolve(gold.query(), referenceDate, options);
            outcomes.put(gold.queryId(), judge(gold, response));
        }
        return outcomes;
    }

    /** 기대 매핑별 매칭 여부와 반환 매핑의 부합 수를 판정한다. */
    private QueryOutcome judge(GoldQuery gold, ResolveResponse response) {
        List<ResolveResponse.ColumnMapping> returned =
                response.getColumnMappings() == null ? List.of() : response.getColumnMappings();

        int matched = 0;
        int codeMatched = 0;
        List<String> missing = new ArrayList<>();
        for (GoldQuery.ExpectedMapping expected : gold.expected()) {
            boolean hit = returned.stream().anyMatch(r -> matches(expected, r));
            if (hit) {
                matched++;
                if (expected.code() != null) {
                    codeMatched++;
                }
            } else {
                missing.add(expected.display());
            }
        }

        long relevantReturned = returned.stream()
                .filter(r -> gold.expected().stream().anyMatch(e -> matches(e, r)))
                .count();

        return new QueryOutcome(
                gold.expected().size(), matched,
                (int) gold.expected().stream().filter(e -> e.code() != null).count(), codeMatched,
                returned.size(), (int) relevantReturned,
                gold.expectsTimeRange(), response.getTimeRange() != null,
                !returned.isEmpty(), missing);
    }

    private boolean matches(GoldQuery.ExpectedMapping expected, ResolveResponse.ColumnMapping returned) {
        return expected.physicalTable().equals(returned.getPhysicalTable())
                && expected.physicalColumn().equals(returned.getPhysicalColumn())
                && (expected.code() == null || expected.code().equals(returned.getCodeValue()));
    }

    private ArmResult aggregate(String arm, List<GoldQuery> goldQueries, Map<String, QueryOutcome> outcomes) {
        int goldTotal = 0;
        int matchedGold = 0;
        int codeGoldTotal = 0;
        int codeGoldMatched = 0;
        int returnedTotal = 0;
        int returnedRelevant = 0;
        int timeExpected = 0;
        int timeHit = 0;
        int mappedQueries = 0;
        double recallSum = 0;

        for (GoldQuery gold : goldQueries) {
            QueryOutcome outcome = outcomes.get(gold.queryId());
            goldTotal += outcome.goldCount();
            matchedGold += outcome.matchedCount();
            codeGoldTotal += outcome.codeGoldCount();
            codeGoldMatched += outcome.codeMatchedCount();
            returnedTotal += outcome.returnedCount();
            returnedRelevant += outcome.relevantReturnedCount();
            if (outcome.timeExpected()) {
                timeExpected++;
                if (outcome.timeHit()) {
                    timeHit++;
                }
            }
            if (outcome.mapped()) {
                mappedQueries++;
            }
            recallSum += outcome.recall();
        }

        return new ArmResult(arm, goldTotal, matchedGold,
                rate(matchedGold, goldTotal),
                round(goldQueries.isEmpty() ? 0 : recallSum / goldQueries.size()),
                rate(returnedRelevant, returnedTotal),
                rate(codeGoldMatched, codeGoldTotal),
                rate(timeHit, timeExpected),
                rate(mappedQueries, goldQueries.size()));
    }

    private double rate(int numerator, int denominator) {
        return denominator == 0 ? 0.0 : round((double) numerator / denominator);
    }

    private double round(double value) {
        return Math.round(value * 10000) / 10000.0;
    }

    /** 질의 1건의 판정 결과 (응용 내부 홀더) */
    private record QueryOutcome(
            int goldCount, int matchedCount,
            int codeGoldCount, int codeMatchedCount,
            int returnedCount, int relevantReturnedCount,
            boolean timeExpected, boolean timeHit,
            boolean mapped, List<String> missing) {

        double recall() {
            return goldCount == 0 ? 0.0 : (double) matchedCount / goldCount;
        }
    }
}
