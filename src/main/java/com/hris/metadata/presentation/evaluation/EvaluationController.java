package com.hris.metadata.presentation.evaluation;

import com.hris.metadata.application.evaluation.RecallEvaluationService;
import com.hris.metadata.application.evaluation.dto.response.EvaluationReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 재현율 평가 API (PRD §8).
 * <p>
 * 동의어·정규화 적용 전(BASELINE)/후(FULL)의 매핑 재현율을 정답셋으로 비교한다.
 */
@Tag(name = "Evaluation", description = "동의어·정규화 재현율 평가 API")
@RestController
@RequestMapping("/api/admin/evaluation")
@RequiredArgsConstructor
public class EvaluationController {

    private final RecallEvaluationService recallEvaluationService;

    @Operation(summary = "재현율 평가", description = "정답셋으로 BASELINE vs FULL 재현율을 비교한다. "
            + "referenceDate 미지정 시 오늘을 기준일로 쓴다.")
    @GetMapping("/recall")
    public ResponseEntity<EvaluationReportResponse> recall(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate) {
        LocalDate effective = referenceDate != null ? referenceDate : LocalDate.now();
        return ResponseEntity.ok(recallEvaluationService.evaluate(effective));
    }
}
