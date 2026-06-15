package com.hris.metadata.infrastructure.catalogsync;

import com.hris.metadata.application.schema.port.PhysicalCatalogSourcePort;
import com.hris.metadata.application.schema.port.PhysicalColumnSnapshot;
import com.hris.metadata.domain.schema.SchemaCatalog;
import com.hris.metadata.domain.schema.SchemaCatalogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * R3 ACL 가드: 외부 소스가 비어 있으면(비활성/미연결) 카탈로그를 건드리지 않고,
 * 스냅샷이 있으면 신규 컬럼을 생성한다.
 */
@ExtendWith(MockitoExtension.class)
class CatalogSyncServiceTest {

    @Mock
    PhysicalCatalogSourcePort catalogSource;
    @Mock
    SchemaCatalogRepository schemaCatalogRepository;

    @Test
    void doesNotWriteWhenSourceEmpty() {
        when(catalogSource.fetchColumns()).thenReturn(List.of());
        CatalogSyncService service = new CatalogSyncService(catalogSource, schemaCatalogRepository);

        int upserted = service.sync();

        assertThat(upserted).isZero();
        verify(schemaCatalogRepository, never()).save(any());
    }

    @Test
    void createsNewColumnWhenAbsent() {
        when(catalogSource.fetchColumns()).thenReturn(List.of(
                new PhysicalColumnSnapshot("settlement", "settlement_status", "varchar", "정산 상태", "redshift")));
        when(schemaCatalogRepository.findByPhysicalTableAndPhysicalColumn(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(schemaCatalogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        CatalogSyncService service = new CatalogSyncService(catalogSource, schemaCatalogRepository);

        int upserted = service.sync();

        assertThat(upserted).isEqualTo(1);
        verify(schemaCatalogRepository).save(any(SchemaCatalog.class));
    }

    @Test
    void skipsUnchangedExistingColumn() {
        SchemaCatalog existing = SchemaCatalog.create(
                UUID.randomUUID(), "settlement", "settlement_status", "varchar", "정산 상태", "redshift");
        when(catalogSource.fetchColumns()).thenReturn(List.of(
                new PhysicalColumnSnapshot("settlement", "settlement_status", "varchar", "정산 상태", "redshift")));
        when(schemaCatalogRepository.findByPhysicalTableAndPhysicalColumn("settlement", "settlement_status"))
                .thenReturn(Optional.of(existing));
        CatalogSyncService service = new CatalogSyncService(catalogSource, schemaCatalogRepository);

        int upserted = service.sync();

        assertThat(upserted).isZero();
        verify(schemaCatalogRepository, never()).save(any());
    }
}
