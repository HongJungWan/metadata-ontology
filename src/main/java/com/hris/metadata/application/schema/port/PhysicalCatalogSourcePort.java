package com.hris.metadata.application.schema.port;

import java.util.List;

/**
 * 외부 물리 스키마 소스 ACL (anti-corruption layer) — 아웃바운드 포트.
 * <p>
 * 미래의 Glue Data Catalog / Redshift {@code information_schema} 는 별도 바운디드 컨텍스트다.
 * 그 외부 표현을 도메인이 받아들일 수 있는 {@link PhysicalColumnSnapshot} 으로 번역하는 경계를 둔다.
 * knowledge-search 의 {@code SettlementSourceAcl} 와 동일한 ACL 패턴.
 * <p>
 * [현재 상태] AWS 미연결. 구현({@code infrastructure.catalogsync.NoOpPhysicalCatalogSourceAdapter})은
 * {@code catalog.sync.enabled=false}(기본값) 동안 빈 목록을 반환하는 no-op 이다. 실제 Glue/JDBC 연동
 * 코드는 자격증명·엔드포인트 확정 후 이 어댑터 안에만 작성한다(도메인/응용은 무영향).
 */
public interface PhysicalCatalogSourcePort {

    /**
     * 외부 소스의 물리 컬럼 스냅샷을 조회한다.
     *
     * @return 컬럼 스냅샷 목록. 비활성/미연결 시 빈 목록(= 동기화 skip).
     */
    List<PhysicalColumnSnapshot> fetchColumns();
}
