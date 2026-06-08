package com.hris.metadata.domain.mapping;

import com.hris.metadata.global.common.BaseEntity;
import com.hris.metadata.shared.ddd.AggregateRoot;
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
 * 용어 ↔ 물리 컬럼 매핑 (조인 엔티티, 애그리거트 루트).
 * <p>
 * [설계 선택] PRD ER 의 TERM_SCHEMA_MAP 조인을 별도 엔티티로 분리하지 않고,
 * SchemaMapping 자체를 (termId, schemaCatalogId, mappingType, codeValueRule) 다대다 조인으로 사용한다.
 * 한 용어가 여러 컬럼에, 한 컬럼이 여러 용어에 걸릴 수 있어 (termId, schemaCatalogId) 조합으로 표현한다.
 * Term/SchemaCatalog 는 다른 애그리거트 루트이므로 객체 참조 없이 식별자(ID)로만 참조한다.
 */
@AggregateRoot
@Entity
@Table(name = "schema_mapping", schema = "meta",
        indexes = {
                @Index(name = "idx_schema_mapping_term_id", columnList = "term_id"),
                @Index(name = "idx_schema_mapping_catalog_id", columnList = "schema_catalog_id")
        })
@Where(clause = "deleted_at IS NULL")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class SchemaMapping extends BaseEntity {

    /** 매핑 ID (PK) */
    @Id
    @Column(name = "schema_mapping_id", nullable = false, columnDefinition = "uuid")
    private UUID schemaMappingId;

    /** 표준 용어 ID (FK) */
    @Column(name = "term_id", nullable = false, columnDefinition = "uuid")
    private UUID termId;

    /** 물리 스키마 카탈로그 ID (FK) */
    @Column(name = "schema_catalog_id", nullable = false, columnDefinition = "uuid")
    private UUID schemaCatalogId;

    /** 매핑 유형 (예: DIRECT, CODE_VALUE) */
    @Column(name = "mapping_type", length = 50)
    private String mappingType;

    /** 코드값 규칙 (예: "PENDING" — 동의어가 특정 코드값을 가리킬 때) */
    @Column(name = "code_value_rule", length = 200)
    private String codeValueRule;

    /**
     * 매핑 생성 (불변식 강제).
     * <p>
     * termId·schemaCatalogId 는 필수다.
     * id 는 application 레이어에서 생성해 주입한다.
     */
    public static SchemaMapping create(UUID schemaMappingId, UUID termId, UUID schemaCatalogId,
                                       String mappingType, String codeValueRule) {
        if (schemaMappingId == null) {
            throw new IllegalArgumentException("schemaMappingId 는 필수입니다.");
        }
        if (termId == null) {
            throw new IllegalArgumentException("termId 는 필수입니다.");
        }
        if (schemaCatalogId == null) {
            throw new IllegalArgumentException("schemaCatalogId 는 필수입니다.");
        }
        return SchemaMapping.builder()
                .schemaMappingId(schemaMappingId)
                .termId(termId)
                .schemaCatalogId(schemaCatalogId)
                .mappingType(mappingType)
                .codeValueRule(codeValueRule)
                .build();
    }

    /** 매핑 필드 수정 (JPA dirty checking) */
    public void update(String mappingType, String codeValueRule) {
        this.mappingType = mappingType;
        this.codeValueRule = codeValueRule;
    }
}
