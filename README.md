# metadata-ontology

도메인 용어와 물리 스키마를 잇는 **경량 온톨로지 메타데이터 계층** (정산/Settlement 도메인).
자연어 질의를 물리 컬럼·코드값·기간으로 매핑하고, LLM 에 줄 스키마 설명 블록을 생성한다.

- **스택**: Java 21 · Spring Boot 3.5.5 · JPA · QueryDSL 5.1.0 · springdoc-openapi 2.7.0
- **포트**: `8096`
- **루트 패키지**: `com.hris.metadata`

> 소비자(P1, knowledge-search)가 REST 로 호출하는 독립 서비스. SQL 조립·실행·랭킹은 소비자 몫이며,
> 본 서비스는 매핑·정규화·패턴·스키마 설명까지만 제공한다. 캐싱도 소비자 측(Caffeine) 책임.

## 로컬 실행

기본/LOCAL 프로필은 **H2 in-memory** 로 부팅한다 (외부 의존성 없음). 부팅 시 정산 사전 시드가 자동 적재된다.

```bash
./gradlew bootRun
# 포트 8096
```

- Health: http://localhost:8096/health
- Swagger UI: http://localhost:8096/swagger-ui.html
- H2 Console: http://localhost:8096/h2-console  (JDBC URL: `jdbc:h2:mem:metadb`, user `sa`, 빈 비밀번호)

```bash
./gradlew test          # 단위 테스트 (NormalizationService, ExpansionService)
./gradlew compileJava   # 컴파일 검증
```

## 매핑·확장 API (§4)

| Method | Path | 설명 |
|---|---|---|
| POST | `/api/resolve` | 정규화+동의어확장+컬럼/코드값 매핑 (P1 한 방 호출) |
| POST | `/api/expand` | 동의어를 표준 용어로 확장 |
| POST | `/api/normalize` | "지난달" 등 상대 기간을 날짜 범위로 |
| POST | `/api/match-sql-pattern` | 키워드 → 컬럼·연산자·값 후보 |
| POST | `/api/prompt-context` | LLM 에 줄 스키마 설명 블록 |

### 관리 API (§5)
- `POST/GET/PUT/DELETE /api/admin/terms`, `/api/admin/terms/{id}/approve`, `/api/admin/synonyms`, `/api/admin/mappings`
- `POST /api/admin/import` (CSV 일괄 임포트, `text/plain`)
- `/api/admin/schema/catalogs`, `/api/admin/schema/code-values`
- `/api/admin/sql-patterns`

## 예시: `/api/resolve`

```bash
curl -X POST http://localhost:8096/api/resolve \
  -H 'Content-Type: application/json' \
  -d '{"query": "미정산 가맹점 지난달"}'
```

응답 (기준일 2026-06-07 가정):

```json
{
  "normalizedQuery": "정산상태 가맹점 2026-05-01~2026-05-31",
  "terms": [
    { "canonical": "정산상태", "matchedSurface": "미정산" },
    { "canonical": "가맹점", "matchedSurface": null }
  ],
  "columnMappings": [
    { "physicalTable": "settlement", "physicalColumn": "settlement_status", "codeValue": null },
    { "physicalTable": "settlement", "physicalColumn": "merchant_id", "codeValue": null }
  ],
  "timeRange": { "from": "2026-05-01", "to": "2026-05-31" },
  "unmapped": []
}
```

> `미정산` 은 동의어 사전에서 표준 용어 `정산상태` 로 확장된다. 컬럼 단위로 `미정산→PENDING` 코드값을
> 더 정밀하게 묶고 싶으면 `/api/match-sql-pattern` (`"미정산" → settlement_status EQ 'PENDING'`) 을 함께 쓴다.

## AWS / PostgreSQL (현재 미연결 — TODO)

- 본 서비스는 아직 외부 DB/AWS 에 연결되지 않았다. **모든 프로필은 H2 로 부팅**한다.
- 실제 PostgreSQL `meta` 스키마 datasource 는 `application.yml` 하단에 `# TODO(AWS):` 주석 블록으로 보관 — 값 확정 후 `prod` 프로필로 활성화한다.
- Glue Data Catalog / Redshift `information_schema` 동기화는 `catalog.sync.enabled: false` (기본) 플래그 뒤에 있으며 `CatalogSyncService.sync()` 에 `// TODO(AWS)` 가드가 있다. 켜도 현재는 미구현이다.
- 자격증명은 환경변수/EC2 환경 파일로 주입한다. **저장소 평문 커밋 금지.**

## 데이터 모델 (§3)

| 엔티티 | 핵심 필드 |
|---|---|
| `Term` | canonicalName(unique), domain, definition, status(ACTIVE/DRAFT/DEPRECATED) |
| `Synonym` | term, surface, type(ABBREVIATION/KOR_ENG/TYPO/COLLOQUIAL) |
| `SchemaCatalog` | physicalTable, physicalColumn, dataType, description, sourceSystem |
| `CodeValue` | schemaCatalog, code, label, synonyms |
| `SchemaMapping` | term, schemaCatalog, mappingType, codeValueRule (Term↔SchemaCatalog 조인) |
| `SqlPattern` | triggerKeywords, columnTarget, operator(EQ/IN/GTE/LTE/LIKE/BETWEEN), valueTemplate, priority |

> `SchemaMapping` 은 (term + schemaCatalog + mappingType + codeValueRule) 단일 조인 엔티티로 구현했다
> (PRD ER 의 TERM_SCHEMA_MAP 역할을 겸함). 한 용어가 여러 컬럼에, 한 컬럼이 여러 용어에 걸릴 수 있다.
