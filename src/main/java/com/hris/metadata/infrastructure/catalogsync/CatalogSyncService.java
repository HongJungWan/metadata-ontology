package com.hris.metadata.infrastructure.catalogsync;

import com.hris.metadata.application.schema.port.PhysicalCatalogSourcePort;
import com.hris.metadata.application.schema.port.PhysicalColumnSnapshot;
import com.hris.metadata.domain.schema.SchemaCatalog;
import com.hris.metadata.domain.schema.SchemaCatalogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 스키마 카탈로그 동기화 서비스 (Glue Data Catalog / Redshift information_schema).
 * <p>
 * PRD §2/§7: 물리 스키마(Redshift/Glue)와 {@code SchemaCatalog} 를 주기 비교·갱신한다.
 * 외부 소스 접근은 ACL 포트({@link PhysicalCatalogSourcePort})에 위임하고, 이 서비스는 받은
 * 스냅샷을 기존 카탈로그와 diff 해 신규/변경 컬럼을 upsert 하는 오케스트레이션만 한다.
 * <p>
 * [현재 상태] AWS 미연결. 포트 구현이 {@code catalog.sync.enabled=false}(기본값) 동안 빈 스냅샷을
 * 반환하므로 이 서비스의 upsert 경로는 휴면 상태다. 실제 Glue/JDBC 연동은 어댑터 안에서만 작성한다.
 */
@Slf4j
@Service
public class CatalogSyncService {

    private final PhysicalCatalogSourcePort catalogSource;
    private final SchemaCatalogRepository schemaCatalogRepository;

    public CatalogSyncService(PhysicalCatalogSourcePort catalogSource,
                              SchemaCatalogRepository schemaCatalogRepository) {
        this.catalogSource = catalogSource;
        this.schemaCatalogRepository = schemaCatalogRepository;
    }

    /**
     * 카탈로그 동기화를 수행한다.
     * <p>
     * 외부 소스 스냅샷을 받아(비활성/미연결이면 빈 목록) 기존 카탈로그와 비교한다. 신규 컬럼은
     * 생성하고, 데이터타입/설명/출처가 바뀐 컬럼은 갱신한다(JPA dirty checking).
     *
     * @return upsert(신규+변경)된 컬럼 수
     */
    public int sync() {
        List<PhysicalColumnSnapshot> snapshots = catalogSource.fetchColumns();
        if (snapshots.isEmpty()) {
            log.debug("Catalog sync — no snapshots to reconcile (source empty). skipping.");
            return 0;
        }

        int upserted = 0;
        for (PhysicalColumnSnapshot s : snapshots) {
            upserted += reconcile(s) ? 1 : 0;
        }
        log.info("Catalog sync — reconciled {} of {} columns.", upserted, snapshots.size());
        return upserted;
    }

    /** 스냅샷 한 건을 기존 카탈로그와 비교해 신규 생성 또는 변경 갱신한다. @return 변경 발생 여부 */
    private boolean reconcile(PhysicalColumnSnapshot s) {
        return schemaCatalogRepository
                .findByPhysicalTableAndPhysicalColumn(s.physicalTable(), s.physicalColumn())
                .map(existing -> updateIfChanged(existing, s))
                .orElseGet(() -> {
                    schemaCatalogRepository.save(SchemaCatalog.create(
                            UUID.randomUUID(), s.physicalTable(), s.physicalColumn(),
                            s.dataType(), s.description(), s.sourceSystem()));
                    return true;
                });
    }

    private boolean updateIfChanged(SchemaCatalog existing, PhysicalColumnSnapshot s) {
        boolean changed = !equalsNullable(existing.getDataType(), s.dataType())
                || !equalsNullable(existing.getDescription(), s.description())
                || !equalsNullable(existing.getSourceSystem(), s.sourceSystem());
        if (changed) {
            existing.update(s.physicalTable(), s.physicalColumn(), s.dataType(), s.description(), s.sourceSystem());
            schemaCatalogRepository.save(existing);
        }
        return changed;
    }

    private static boolean equalsNullable(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
