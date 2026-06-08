package com.hris.metadata.domain.schema;

import com.hris.metadata.global.common.BaseEntity;
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
 * 코드값 사전.
 * <p>
 * 특정 컬럼이 가질 수 있는 코드값(예: 정산상태 PENDING/SETTLED)과 그 한글 라벨·동의어를 담는다.
 * 스키마 카탈로그는 식별자(schemaCatalogId)로만 참조한다 (애그리거트 간 ID 참조).
 */
@Entity
@Table(name = "code_value", schema = "meta",
        indexes = @Index(name = "idx_code_value_catalog_id", columnList = "schema_catalog_id"))
@Where(clause = "deleted_at IS NULL")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class CodeValue extends BaseEntity {

    /** 코드값 ID (PK) */
    @Id
    @Column(name = "code_value_id", nullable = false, columnDefinition = "uuid")
    private UUID codeValueId;

    /** 스키마 카탈로그 ID (FK) */
    @Column(name = "schema_catalog_id", nullable = false, columnDefinition = "uuid")
    private UUID schemaCatalogId;

    /** 코드 (예: PENDING) */
    @Column(name = "code", nullable = false, length = 100)
    private String code;

    /** 라벨 (예: 미정산) */
    @Column(name = "label", length = 200)
    private String label;

    /** 코드값 동의어 (콤마 구분 문자열, 예: "미정산,대기") */
    @Column(name = "synonyms", length = 500)
    private String synonyms;

    /** 코드값 필드 수정 (JPA dirty checking) */
    public void update(String code, String label, String synonyms) {
        this.code = code;
        this.label = label;
        this.synonyms = synonyms;
    }
}
