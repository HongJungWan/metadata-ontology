# ADR — 애그리거트 경계 결정

> 상태: Accepted (2026-06-08). 컨텍스트: DDD 적합성 리팩토링(refactor/ddd-conformance).

## 1. Synonym 과 CodeValue 를 독립 @AggregateRoot 로 둔다

### 결정
`Synonym`(term-vocab)·`CodeValue`(schema-catalog)를 각각 **독립 애그리거트 루트(`@AggregateRoot`)**로
유지한다. `Term`/`SchemaCatalog` 의 *내부 엔티티(`@AggregateInternal`)*로 흡수하지 않는다. 부모는
식별자(`termId`/`schemaCatalogId`)로만 참조한다.

### 맥락 / 근거
- **개별 라이프사이클·CRUD**: Synonym·CodeValue 는 부모와 독립적으로 생성/삭제(소프트삭제)된다
  (`POST /api/admin/synonyms`, `DELETE /api/admin/synonyms/{id}`, code-value 동일). 부모를 거치지 않는
  자체 진입점이 있다.
- **독립 조회 경로**: 동의어 확장·코드값 보강은 부모 객체 그래프를 로드하지 않고 surface/카탈로그ID로
  바로 조회한다(N+1 회피, `findCandidatesBySurface` 등). 내부 엔티티로 만들면 항상 루트를 거쳐야 해
  조회가 무거워진다.
- **불변식 공유 없음**: "한 Term 의 동의어 개수/조합" 같은 *루트가 지켜야 할 트랜잭션 불변식*이 없다.
  애그리거트로 묶는 본래 목적(불변식의 일관성 경계)이 성립하지 않으므로 묶을 이유가 없다.
- **작은 애그리거트 원칙**: 부모에 컬렉션으로 흡수하면 애그리거트가 커지고 동시성 충돌 면이 넓어진다.
  Vaughn Vernon "Effective Aggregate Design"의 *작게 설계하라* 지침에 부합.

### 대안 (기각)
- **Term 의 내부 엔티티로**: 위 라이프사이클/조회 독립성 때문에 부적합. CRUD 진입점을 루트 메서드로
  강제하면 API/조회가 부자연스러워진다.
- **plain @Entity (마커 없음)**: 마커 누락은 ArchUnit `DOMAIN_ENTITY_MARKED`(도메인 @Entity 는
  @AggregateRoot/@AggregateInternal 필수) 위반. 둘 중 하나를 골라야 하며, 위 근거로 AggregateRoot 선택.

### 결과
- ArchUnit `ID_REFERENCE_BETWEEN_AGGREGATES` 가 부모를 객체 참조하지 않음을 강제(이미 ID 참조).
- 트레이드오프: 참조 무결성(존재 여부)은 DB FK 가 아니라 application 서비스의 `findById/existsBy`
  존재 검증으로 보장한다(현 구현 그대로).

## 2. 도메인 이벤트 미도입 (deferred)

용어 승인·폐기 등 상태변경에 도메인 이벤트를 도입하지 않는다 — 현재 **소비자(이벤트 핸들러/메시지 버스)가 없고**,
도메인에서의 이벤트 발행은 하네스가 차단(`ApplicationEventPublisher`/`org.springframework.context.event`
도메인 임포트 금지)한다. 필요해지면 그때 application 레이어 발행 + 아웃박스로 도입한다.

## 3. QueryDSL 조회 대상 컬럼을 VO로 감싸지 않는다 (deferred)

`canonicalName`/`physicalTable`/`physicalColumn`/`code`/`surface`/`triggerKeywords`/`valueTemplate` 은
QueryDSL where/join 의 타깃 컬럼이다. VO 로 감싸면 Q타입·바인딩·`@Convert` 비용과 조인 깨짐 위험이 커서,
실효 대비 리스크가 높다. 원시 타입 + 팩토리/컴팩트 생성자 검증으로 불변식을 지키고 VO 화는 보류한다.
