# 재현율 평가 — 동의어·정규화 적용 전후 비교 (PRD §8)

동의어 사전·기간 정규화 계층이 검색 재현율에 기여하는 정도를 정답셋으로 측정한다.
두 평가 대상(arm) 모두 **동일한 `ResolveService` 코드 경로**를 타며, 단계 토글(`ResolveOptions`)만 다르다.

- **BASELINE** = `ResolveOptions.rawBaseline()` — 동의어 확장·기간 정규화 미적용. 원문 토큰을 그대로 표준 용어 사전에 조회.
- **FULL** = `ResolveOptions.full()` — 현행 `/api/resolve` 동작 (정규화 → 동의어 확장 → 컬럼·코드값 매핑).

## 지표 정의

질의 q 의 정답 집합 G(q) = 기대 (table, column[, code]) 항목들. 시스템 반환 R(q) = resolve 응답 `columnMappings`.

- 정답 항목 g 의 매칭: ∃ r ∈ R(q) — r.table = g.table ∧ r.column = g.column ∧ (g.code 없음 ∨ g.code = r.codeValue)
- **micro recall** = Σ_q |매칭된 G(q)| / Σ_q |G(q)| — 헤드라인 지표
- **macro recall** = (1/N) Σ_q (매칭된 G(q) / |G(q)|)
- **precision** = Σ_q |G(q) 에 부합하는 R(q)| / Σ_q |R(q)| — **참고 지표**. 정답셋은 기대 매핑의
  완전 열거가 아니다(예: "가맹점"은 8개 테이블의 merchant_id 로 다중 매핑되지만 정답셋은 대표 1건만 기재).
  따라서 precision 낮음 = 오답이 아니라 "정답셋에 안 적은 정상 매핑"일 수 있다.
- **코드값 적중률** = code 가 명시된 정답 중 코드까지 정확히 일치한 비율
- **기간 인식률** = `expectsTimeRange=true` 질의 중 timeRange 가 해석된 비율
- **매핑 커버리지** = 컬럼 매핑이 1건 이상 반환된 질의 비율 (PRD §7)

## 정답셋

`src/main/resources/evaluation/gold_queries.csv` — 60개 질의. 구성:

| 구간 | 수 | 의도 |
|---|---|---|
| 표준 용어만 (Q001–Q015) | 15 | BASELINE 도 성공 → 비교 기준선이 0 이 아닌 현실적 수치가 되게 |
| 동의어·약어·한영·오타 (Q016–Q040) | 25 | 동의어 계층 기여분 측정 |
| 코드값 표면형 (Q041–Q050) | 10 | "미정산"→PENDING 류 코드값 보강 측정 |
| 기간 표현 (Q051–Q060) | 10 | "지난달" 류 정규화 측정 |

- 기간 기대값은 절대 날짜 대신 `expectsTimeRange` boolean 으로만 판정 → 실행일과 무관하게 결정론적.
- 질의는 공백 토큰 형태로 작성한다. resolve 의 매칭이 공백 토큰 단위 정확 일치이므로
  조사·어미가 붙은 자연문(예: "미정산인 가맹점들")은 이 평가의 범위 밖이다 — **토큰 단위 평가**라는
  한계를 의도적으로 둔다(형태소 분석은 비목표, PRD §1.2).
- 정답셋 무결성(컬럼·코드 실존, ID 유일)은 `SeedDictionaryIntegrityTest` 가 시드와 상호참조로 강제한다.

## 실행 방법

```bash
# 회귀 게이트 (기준일 고정 2026-06-07 → 결정론적) + 마크다운 리포트 생성
./gradlew test --tests '*RecallEvaluationIntegrationTest'
# → build/reports/evaluation/recall-report.md

# 라이브 (Swagger /swagger-ui.html 또는)
curl "http://localhost:8096/api/admin/evaluation/recall?referenceDate=2026-06-07"
```

## 결과 스냅샷 (시드: 용어 184 · 매핑 152 · 동의어 292 / 정답셋 60문항 기준, 2026-06)

| 지표 | BASELINE | FULL |
|---|---|---|
| micro recall | 0.3438 | 1.0 |
| macro recall | 0.3583 | 1.0 |
| precision (참고) | 0.6471 | 0.5393 |
| 코드값 적중률 | 0.1364 | 1.0 |
| 기간 인식률 | 0.0 | 1.0 |
| 매핑 커버리지 | 0.3667 | 1.0 |

회귀 게이트(통합 테스트 단언): `FULL micro recall > BASELINE micro recall`,
`FULL micro recall ≥ 0.95`, `FULL 코드값 적중률 ≥ 0.90`, `FULL 기간 인식률 ≥ 0.90`,
`BASELINE micro recall ≤ 0.60`.

> 해석 시 주의: 이 수치는 **이 레포의 시드·정답셋에 대한** 측정값이다. FULL=1.0 은 정답셋의
> 질의가 모두 사전으로 해석 가능하게 설계되었음을 뜻하며, 임의의 실사용 질의에 대한 일반화
> 성능 주장이 아니다. 실사용 재현율은 P1(knowledge-search)의 SearchLog 기반 평가(후속 과제)로 본다.

## 후속 과제

- P1(knowledge-search) 통합 평가: 매핑 적용/미적용 검색을 나란히 놓고 P1 의 채점 점수(judgedScore)와 함께 보는 엔드투엔드 재현율 (PRD §8 후반부) — 이 레포 범위 밖.
