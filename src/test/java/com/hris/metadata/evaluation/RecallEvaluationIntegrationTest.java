package com.hris.metadata.evaluation;

import com.hris.metadata.application.evaluation.RecallEvaluationService;
import com.hris.metadata.application.evaluation.dto.response.EvaluationReportResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 재현율 평가 엔드투엔드 통합 테스트 (H2 + CSV 시드 + 정답셋).
 * <p>
 * 동의어·정규화 적용 전(BASELINE)/후(FULL)를 같은 resolve 경로로 비교해
 * 동의어 사전 계층의 재현율 기여를 회귀 게이트로 고정한다. 기준일을 고정해 결정론적이다.
 * 실행 후 비교표를 {@code build/reports/evaluation/recall-report.md} 로 남긴다.
 */
@SpringBootTest
@ActiveProfiles("local")
class RecallEvaluationIntegrationTest {

    /** 기간 정규화 기준일 (고정 — 결과 결정론) */
    private static final LocalDate REFERENCE_DATE = LocalDate.of(2026, 6, 7);

    @Autowired
    private RecallEvaluationService recallEvaluationService;

    @Test
    @DisplayName("동의어·정규화 적용(FULL)이 미적용(BASELINE)보다 재현율이 높고 임계값을 만족한다")
    void fullOutperformsBaseline() throws IOException {
        EvaluationReportResponse report = recallEvaluationService.evaluate(REFERENCE_DATE);

        writeMarkdownReport(report);

        // 핵심 단언: 동의어 사전 계층의 기여
        assertThat(report.full().microRecall())
                .as("FULL 이 BASELINE 보다 micro recall 이 높아야 한다")
                .isGreaterThan(report.baseline().microRecall());

        // 임계값 (정답셋·시드 고정 → 결정론적 실측 기준)
        assertThat(report.full().microRecall()).as("FULL micro recall").isGreaterThanOrEqualTo(0.95);
        assertThat(report.full().codeValueHitRate()).as("FULL 코드값 적중률").isGreaterThanOrEqualTo(0.90);
        assertThat(report.full().timeRangeHitRate()).as("FULL 기간 인식률").isGreaterThanOrEqualTo(0.90);
        assertThat(report.baseline().microRecall()).as("BASELINE micro recall").isLessThanOrEqualTo(0.60);

        // 퍼지 무회귀: 정확 매칭이 이미 커버하는 메인 셋에서 FUZZY 가 FULL 을 떨어뜨리지 않는다.
        assertThat(report.fuzzy().microRecall())
                .as("FUZZY 는 메인 셋에서 FULL 대비 무회귀")
                .isGreaterThanOrEqualTo(report.full().microRecall());
    }

    @Test
    @DisplayName("퍼지 폴백은 정확 매칭이 놓치는 오타/OOV 표면형을 회복한다 (FUZZY > FULL)")
    void fuzzyRecoversOovThatExactMisses() {
        EvaluationReportResponse oov =
                recallEvaluationService.evaluate(REFERENCE_DATE, "evaluation/gold_oov.csv");

        // 정확 매칭(FULL)은 오타/OOV 를 거의 못 잡고, 퍼지(FUZZY)가 회복한다 — 비대칭 설계의 MO 측 실효성.
        assertThat(oov.full().microRecall()).as("FULL 은 오타/OOV 를 거의 못 잡음").isLessThanOrEqualTo(0.20);
        assertThat(oov.fuzzy().matchedGold())
                .as("FUZZY 가 FULL 보다 더 많은 기대 매핑을 회복")
                .isGreaterThan(oov.full().matchedGold());
        assertThat(oov.fuzzy().microRecall()).as("FUZZY 오타/OOV 회복률").isGreaterThanOrEqualTo(0.60);
    }

    private void writeMarkdownReport(EvaluationReportResponse report) throws IOException {
        StringBuilder md = new StringBuilder();
        md.append("# 재현율 평가 리포트 (BASELINE vs FULL)\n\n");
        md.append("- 기준일: ").append(REFERENCE_DATE).append(" · 질의 수: ").append(report.queryCount()).append('\n');
        md.append("- BASELINE = 동의어 확장·기간 정규화 미적용 (원문 토큰 그대로) · FULL = 현행 resolve\n\n");
        md.append("| 지표 | BASELINE | FULL | FUZZY |\n|---|---|---|---|\n");
        appendMetric(md, "micro recall", report.baseline().microRecall(), report.full().microRecall(),
                report.fuzzy().microRecall());
        appendMetric(md, "macro recall", report.baseline().macroRecall(), report.full().macroRecall(),
                report.fuzzy().macroRecall());
        appendMetric(md, "precision (참고)", report.baseline().precision(), report.full().precision(),
                report.fuzzy().precision());
        appendMetric(md, "코드값 적중률", report.baseline().codeValueHitRate(), report.full().codeValueHitRate(),
                report.fuzzy().codeValueHitRate());
        appendMetric(md, "기간 인식률", report.baseline().timeRangeHitRate(), report.full().timeRangeHitRate(),
                report.fuzzy().timeRangeHitRate());
        appendMetric(md, "매핑 커버리지", report.baseline().mappedQueryRate(), report.full().mappedQueryRate(),
                report.fuzzy().mappedQueryRate());
        md.append("\n## BASELINE 에서 놓친 질의 (동의어 계층 기여분)\n\n");
        md.append("| ID | 질의 | BASELINE | FULL | BASELINE 누락 |\n|---|---|---|---|---|\n");
        report.perQuery().stream()
                .filter(q -> q.baselineRecall() < q.fullRecall())
                .forEach(q -> md.append("| ").append(q.queryId()).append(" | ").append(q.query())
                        .append(" | ").append(q.baselineRecall()).append(" | ").append(q.fullRecall())
                        .append(" | ").append(String.join(" · ", q.missingInBaseline())).append(" |\n"));

        Path reportPath = Path.of("build", "reports", "evaluation", "recall-report.md");
        Files.createDirectories(reportPath.getParent());
        Files.writeString(reportPath, md.toString(), StandardCharsets.UTF_8);
    }

    private void appendMetric(StringBuilder md, String name, double baseline, double full, double fuzzy) {
        md.append("| ").append(name).append(" | ").append(baseline).append(" | ").append(full)
                .append(" | ").append(fuzzy).append(" |\n");
    }
}
