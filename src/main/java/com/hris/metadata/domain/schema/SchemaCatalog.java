package com.hris.metadata.domain.schema;

import com.hris.metadata.global.common.BaseEntity;
import com.hris.metadata.shared.ddd.AggregateRoot;
import com.hris.metadata.shared.ddd.Subdomain;
import com.hris.metadata.shared.ddd.SubdomainType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Where;

import java.util.UUID;

/**
 * 물리 스키마 카탈로그 (애그리거트 루트).
 * <p>
 * 물리 테이블·컬럼 정보를 담는다. 운영 환경에서는 Redshift/Glue 와 주기 동기화 대상이다.
 */
@AggregateRoot
@Subdomain(SubdomainType.SUPPORTING)
@Entity
@Table(name = "schema_catalog", schema = "meta",
        indexes = @Index(name = "idx_schema_catalog_table_column", columnList = "physical_table, physical_column"))
@Where(clause = "deleted_at IS NULL")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class SchemaCatalog extends BaseEntity {

    /** 스키마 카탈로그 ID (PK) */
    @Id
    @Column(name = "schema_catalog_id", nullable = false, columnDefinition = "uuid")
    private UUID schemaCatalogId;

    /** 물리 테이블명 (예: settlement) */
    @Column(name = "physical_table", nullable = false, length = 200)
    private String physicalTable;

    /** 물리 컬럼명 (예: settlement_amount) */
    @Column(name = "physical_column", nullable = false, length = 200)
    private String physicalColumn;

    /** 데이터 타입 (예: numeric, varchar, date) */
    @Column(name = "data_type", length = 100)
    private String dataType;

    /** 컬럼 설명 */
    @Column(name = "description", length = 1000)
    private String description;

    /** 출처 시스템 (예: redshift, glue) */
    @Column(name = "source_system", length = 100)
    private String sourceSystem;

    /**
     * 스키마 카탈로그 생성 (불변식 강제).
     * <p>
     * physicalTable·physicalColumn 은 공백일 수 없다.
     * id 는 application 레이어에서 생성해 주입한다.
     */
    public static SchemaCatalog create(UUID schemaCatalogId, String physicalTable, String physicalColumn,
                                       String dataType, String description, String sourceSystem) {
        if (schemaCatalogId == null) {
            throw new IllegalArgumentException("schemaCatalogId 는 필수입니다.");
        }
        if (physicalTable == null || physicalTable.isBlank()) {
            throw new IllegalArgumentException("physicalTable 은 공백일 수 없습니다.");
        }
        if (physicalColumn == null || physicalColumn.isBlank()) {
            throw new IllegalArgumentException("physicalColumn 은 공백일 수 없습니다.");
        }
        return SchemaCatalog.builder()
                .schemaCatalogId(schemaCatalogId)
                .physicalTable(physicalTable)
                .physicalColumn(physicalColumn)
                .dataType(dataType)
                .description(description)
                .sourceSystem(sourceSystem)
                .build();
    }

    /** 카탈로그 필드 수정 (JPA dirty checking) */
    public void update(String physicalTable, String physicalColumn, String dataType,
                       String description, String sourceSystem) {
        this.physicalTable = physicalTable;
        this.physicalColumn = physicalColumn;
        this.dataType = dataType;
        this.description = description;
        this.sourceSystem = sourceSystem;
    }
}
