# Context Map — metadata-ontology

> 전략적 설계(서브도메인/바운디드 컨텍스트/관계)를 한 장으로. 전술 설계는 코드와 CLAUDE.md.

## 바운디드 컨텍스트

이 서비스는 **단일 바운디드 컨텍스트**(Metadata Ontology)다. 내부는 네 개의 응집 모듈로 나뉘며, 모두 같은
`meta` 스키마·같은 트랜잭션 경계·같은 유비쿼터스 언어를 공유한다(별도 컨텍스트가 아니라 *모듈*).

| 모듈 | 패키지 | 서브도메인 | 책임 |
|---|---|---|---|
| term-vocab | `domain.term` | **CORE** | 표준 용어(Term)·동의어(Synonym). 같은 개념의 다양한 표현을 정식 명칭으로 모으는 SSOT. |
| schema-catalog | `domain.schema` | SUPPORTING | 물리 테이블/컬럼(SchemaCatalog)·코드값(CodeValue). |
| mapping | `domain.mapping` | SUPPORTING | 용어 ↔ 물리 컬럼 조인(SchemaMapping). |
| sql-patterns | `domain.pattern` | GENERIC | 트리거 키워드 → 컬럼/연산자/값 후보(SqlPattern). 범용 규칙, 외부로 대체 가능. |

서브도메인 분류는 `@Subdomain(CORE|SUPPORTING|GENERIC)` 마커로 코드에 표기되고, ArchUnit
`CORE_NOT_DEPEND_ON_GENERIC` 가 "CORE 가 GENERIC 에 의존 금지"를 강제한다(Term/Synonym → SqlPattern 금지).

## 외부 관계

```
┌─────────────────────────┐        REST (HTTP/JSON)        ┌──────────────────────────┐
│   knowledge-search (P1)  │ ─────────  consumes  ────────▶ │   metadata-ontology (P2)   │
│   (자연어→SQL 소비자)     │   /api/resolve · /expand ...   │   = 우리(공급자, OHS)       │
└─────────────────────────┘                                 └──────────────────────────┘
```

- **관계 패턴**: 우리는 **Open Host Service (OHS)** — 안정적인 공개 REST API(`/api/resolve`,
  `/api/expand`, `/api/normalize`, `/api/match-sql-pattern`, `/api/prompt-context`)로 다운스트림에
  매핑 능력을 공개한다. JSON 계약은 published language 역할.
- **Published Language 코드 표기**: 발행 언어 계약 타입은 `@PublishedLanguage` 마커로 명시한다
  (`shared/ddd/PublishedLanguage`). 현재 knowledge-search 가 실제 소비하는 계약에 부착:
  `ResolveResponse`(/api/resolve ↔ KS `MetadataResolveResult`)·`SchemaCatalogResponse`(/api/admin/schema/catalogs
  ↔ KS `MetadataSchemaClient`). 이 타입의 필드 변경은 다운스트림 협의 필수. (그 외 /expand·/normalize·
  /match-sql-pattern 는 도메인 결과 record 를 직접 직렬화 — 향후 전용 응답 DTO 분리 검토 대상.)
- **knowledge-search 와의 방향**: 우리가 **Upstream(공급자)**, knowledge-search 가 **Downstream(소비자)**.
  소비자가 우리 모델을 그대로 받아쓰는 Conformist 성격(코드값·컬럼명을 그대로 사용).
- **외부 ACL 불필요**: 운영 카탈로그 동기화(Glue/Redshift)는 현재 비활성(`catalog.sync.enabled=false`)이고,
  관리 API는 내부 CRUD다. 외부 모델을 우리 도메인으로 번역하는 부패 방지 계층(ACL)이 필요한 인바운드
  통합이 아직 없다 — 따라서 ACL을 두지 않는다. (운영 동기화를 켜는 시점에 catalog-sync 어댑터를 ACL로
  격상하는 것이 후보.)

## 모듈 간 결합 규칙 (내부)

- 애그리거트 루트 간 참조는 **ID로만**(객체 참조 금지). 조인 결과는 도메인 record(`ColumnMapping`/
  `CodeValueCandidate`/`SynonymMatch`)로 평면화해 포트가 반환.
- CORE(term-vocab)는 GENERIC(sql-patterns)에 의존하지 않는다(ArchUnit 강제).
- 오케스트레이션(`ResolveService`/`PromptContextService`)은 application 레이어에서 여러 모듈의 포트를 조율.
