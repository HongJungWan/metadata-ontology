package com.hris.metadata.domain.schema.sync;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 스키마 카탈로그 동기화 서비스 (Glue Data Catalog / Redshift information_schema).
 * <p>
 * PRD §2/§7: 물리 스키마(Redshift/Glue)와 SchemaCatalog 를 주기 비교·갱신한다.
 * <p>
 * [현재 상태] AWS 미연결. {@code catalog.sync.enabled=false} (기본값) 일 때는 동작하지 않는다.
 * 실제 Glue/Redshift 연동 코드는 자격증명·엔드포인트 확정 후 작성한다.
 */
@Slf4j
@Service
public class CatalogSyncService {

    /** 동기화 활성화 플래그 (기본 false — AWS 미연결) */
    @Value("${catalog.sync.enabled:false}")
    private boolean syncEnabled;

    /**
     * 카탈로그 동기화를 수행한다.
     * <p>
     * 플래그가 꺼져 있으면 즉시 반환(guard) 한다. 켜져 있어도 현재는 미구현이다.
     */
    public void sync() {
        if (!syncEnabled) {
            log.debug("Catalog sync disabled (catalog.sync.enabled=false) — skipping.");
            return;
        }

        // TODO(AWS): Glue GetTables / Redshift information_schema.columns 를 조회해
        //  SchemaCatalog 와 diff 한 뒤 신규/변경 컬럼을 upsert 한다.
        //  - GLUE_DATABASE / AWS_REGION 환경변수로 Glue 클라이언트 구성
        //  - 또는 PostgreSQL information_schema 직접 조회 (meta 스키마 datasource)
        //  - 불일치 발견 시 알림(PRD §7 스키마 불일치) 신호 전송
        //  나중에 실제 값 기재.
        log.warn("Catalog sync enabled but not yet implemented (AWS not connected).");
    }
}
