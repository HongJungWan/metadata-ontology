# CLAUDE.md — metadata-ontology

> 상세 설계는 `.claude/docs/prd-metadata-ontology.md`. 이 파일은 **작업 원칙 + 빠른 참조**만 둔다(중복 금지).

## Working Principles — Karpathy 4원칙 (최우선)

> [Andrej Karpathy의 LLM 코딩 함정 관찰](https://github.com/forrestchang/andrej-karpathy-skills) 기반. 이 프로젝트의 *모든* 코드 작업에 기본 적용. 사소한 작업은 우회 가능하나, 의심스러우면 먼저 적용한다. **속도보다 신중함** 쪽으로 편향.

### 1. 코딩 전에 생각하라
가정은 명시하고 불확실하면 질문한다(`AskUserQuestion`). 해석이 여럿이면 *모두* 제시하고 임의로 고르지 않는다. 더 단순한 길이 있으면 push back 한다. 불분명하면 멈추고, 무엇이 혼란스러운지 명명한다.

### 2. 단순함 우선
문제를 푸는 *최소* 코드만 쓴다. 요청 없는 기능·일회성 추상화·불필요한 "유연성"·발생 불가능한 시나리오 방어코드는 넣지 않는다. 과설계 같으면 다시 쓴다.

### 3. 외과적 변경
꼭 필요한 곳만 건드린다. 인접 코드·주석·포맷을 임의로 "개선"하거나 멀쩡한 것을 리팩토링하지 않는다. *기존 스타일*을 따른다. 본인 변경으로 생긴 고아 코드만 정리하고, 원래부터 있던 dead code는 언급만 한다.

### 4. 목표 주도 실행
작업을 *검증 가능한* 목표로 바꾸고 통과할 때까지 루프한다. "동작하게 만들어" 같은 약한 기준은 금지. 다단계는 `단계 → 검증방법`을 먼저 적는다.

---

## 한 줄 정의
도메인 용어 ↔ 물리 스키마를 잇는 **공유 메타데이터 계층**. 자연어 질의를 컬럼·코드값으로 매핑하고 LLM용 스키마 설명을 만든다. 포트 8096, 루트 패키지 `com.hris.metadata`.

## 명령
| 목적 | 명령 |
|---|---|
| 로컬 실행(H2) | `./gradlew bootRun` |
| 테스트 | `./gradlew test` |
| 빌드 | `./gradlew clean build` |

- 헬스 `/health` · Swagger `/swagger-ui.html`
- 핵심 API: `POST /api/resolve`(한 방 호출) · `/api/expand` · `/api/normalize` · `/api/match-sql-pattern` · `/api/prompt-context`
- 관리: `TermAdminController` CRUD + `POST /api/admin/import`(CSV)
- 예시: `POST /api/resolve {"query":"미정산 가맹점 지난달"}` → `settlement_status` 컬럼에 `codeValue:"PENDING"` + 기간 정규화

## 작업 시 주의 (이 프로젝트 고유)
- **AWS·운영 DB 미연동**: 운영 PostgreSQL `meta` 스키마와 Glue/Redshift `information_schema` 동기화는 `application.yml`에 `# TODO(AWS)` 주석. `catalog.sync.enabled` 기본 `false`. 실값은 사용자가 나중에 기재 — **임의로 켜지 마라.**
- **H2 시드**: `DataSeeder`(`@Profile("local")`)가 정산 사전(용어 6/동의어 7/코드값 4/패턴 4)을 적재. `create-drop`이라 재시작 시 초기화.
- **매핑 모델**: `Term↔SchemaCatalog` 조인은 단일 `SchemaMapping` 엔티티 하나로 둔다(별도 `TermSchemaMap` 없음).
- **`/resolve`의 코드값**: 표면형이 코드값 동의어면(예: "미정산"→PENDING) `columnMappings[].codeValue`에 채워 내려준다(PRD §4.1). 용어와 무관한 순수 코드값 토큰은 `/match-sql-pattern`이 담당.
- **DDL**: H2 `create-drop`. 운영은 `validate` + 별도 스키마 관리.

## DDD 하네스 (opinionated-harness-template, DEFAULT 설정)

> 코드 작성·수정 시 `.claude/hooks/harness.mjs`가 자동 검사한다(설정: `.claude/hooks/harness.config.json`, **DEFAULT/verbatim**). 상세는 `docs/HARNESS.md`. 카파시 4원칙과 같은 철학. CI 정밀 게이트는 ArchUnit(`.github/workflows/ddd-archunit.yml`).

### 패키지 구조 (실용 레이어드 헥사고날 + DDD, 루트 `com.hris.metadata`)
| 레이어 | 글롭 | 내용 |
|---|---|---|
| `shared.ddd` | — | 마커 어노테이션 5종 (`@AggregateRoot` 등) |
| `domain` | `**/domain/**` | 엔티티·VO·도메인 record·**포트(plain interface)**. 바깥 레이어 의존 금지. |
| `application` | `**/application/**` | `@Service` 오케스트레이션. 도메인 포트에만 의존(+ 서로). 결과 홀더(NormalizationResult/ExpansionResult/SqlPatternMatch/ImportResult) 포함. |
| `infrastructure` | `**/infrastructure/**` | 포트 어댑터 `*RepositoryImpl`(+ Spring Data `*JpaRepository`/QueryDSL), config, DataSeeder, catalogsync. |
| `presentation` | `**/controller/**`,`**/presentation/**` | 컨트롤러 + 모든 DTO(request/response, TimeRange/ResolveResponse 포함). |

- **애그리거트 루트(`@AggregateRoot`)**: `Term`·`SchemaCatalog`·`SchemaMapping`·`SqlPattern`만. `Synonym`/`CodeValue`는 plain `@Entity`(여러 레이어에서 읽힘).
- **루트 간 참조는 ID로**: 엔티티는 다른 루트를 객체 필드로 참조하지 않는다(`@ManyToOne` 제거, `termId`/`schemaCatalogId`만). 조인 결과는 도메인 record(`ColumnMapping`/`CodeValueCandidate`/`SynonymMatch`)로 평면화해 포트가 반환.
- **DIP**: 도메인엔 포트 인터페이스만, 구현(`*RepositoryImpl`)은 infrastructure. `*RepositoryImpl`/`*RepositoryCustomImpl` 파일명은 domain 에서 금지.
- **차단(block) 규칙**: domain 에 `@Service`/`@Transactional`/`@Setter`/`@Data`/public setter/`.now()`/`UUID.randomUUID()` 금지 · 빈약 엔티티 금지 · domain→application/infra 임포트 금지 · application→infra 임포트 금지 · 필드주입(`@Autowired`/`@Value` 필드) 금지(생성자 주입) · `./gradlew`만 사용. (`UUID.randomUUID()`/`LocalDate.now()`는 application/infra 에선 허용.)
- 현재 전 소스 **차단 0건**, `./gradlew clean build`·ArchUnit GREEN.
- **커맨드**: `/ddd-review` · `/ddd-fix` · `/verify`. 훅 실행에 Node.js 필요.
