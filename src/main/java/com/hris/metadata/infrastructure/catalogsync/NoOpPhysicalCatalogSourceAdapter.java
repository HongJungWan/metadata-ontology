package com.hris.metadata.infrastructure.catalogsync;

import com.hris.metadata.application.schema.port.PhysicalCatalogSourcePort;
import com.hris.metadata.application.schema.port.PhysicalColumnSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 외부 물리 스키마 소스 어댑터 — 현재는 no-op ({@link PhysicalCatalogSourcePort} 구현).
 * <p>
 * AWS 미연결 상태에서는 {@code catalog.sync.enabled=false}(기본값) 가드로 빈 목록을 반환한다.
 * 실제 Glue {@code GetTables} / Redshift {@code information_schema.columns} 조회·번역 코드는
 * 자격증명·엔드포인트 확정 후 이 클래스 안에만 작성한다 — 도메인/응용 계약은 그대로 둔다.
 * (ACL 경계는 지금 정의, 구현은 연기: ADR §4)
 */
@Slf4j
@Component
public class NoOpPhysicalCatalogSourceAdapter implements PhysicalCatalogSourcePort {

    private final boolean syncEnabled;

    public NoOpPhysicalCatalogSourceAdapter(@Value("${catalog.sync.enabled:false}") boolean syncEnabled) {
        this.syncEnabled = syncEnabled;
    }

    @Override
    public List<PhysicalColumnSnapshot> fetchColumns() {
        if (!syncEnabled) {
            log.debug("Catalog source disabled (catalog.sync.enabled=false) — returning empty snapshot.");
            return List.of();
        }
        // TODO(AWS): Glue GetTables / Redshift information_schema.columns 를 조회해
        //  PhysicalColumnSnapshot 목록으로 번역한다.
        //  - GLUE_DATABASE / AWS_REGION 환경변수로 Glue 클라이언트 구성, 또는
        //  - PostgreSQL information_schema 직접 조회(meta 스키마 datasource)
        //  나중에 실제 값 기재.
        log.warn("Catalog source enabled but not yet implemented (AWS not connected) — returning empty snapshot.");
        return List.of();
    }
}
