package com.hris.metadata.domain.schema;

import com.hris.metadata.domain.schema.vo.Code;
import com.hris.metadata.domain.schema.vo.CodeValueId;
import com.hris.metadata.domain.schema.vo.CodeValueSynonyms;
import com.hris.metadata.domain.schema.vo.Label;
import com.hris.metadata.domain.schema.vo.SchemaCatalogId;
import com.hris.metadata.global.common.BaseEntity;
import com.hris.metadata.shared.ddd.AggregateRoot;
import com.hris.metadata.shared.ddd.Subdomain;
import com.hris.metadata.shared.ddd.SubdomainType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
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
@AggregateRoot
@Subdomain(SubdomainType.SUPPORTING)
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
    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "code_value_id", nullable = false, columnDefinition = "uuid"))
    private CodeValueId codeValueId;

    /** 스키마 카탈로그 ID (FK) */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "schema_catalog_id", nullable = false, columnDefinition = "uuid"))
    private SchemaCatalogId schemaCatalogId;

    /** 코드 (예: PENDING) */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "code", nullable = false, length = 100))
    private Code code;

    /** 라벨 (예: 미정산) */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "label", length = 200))
    private Label label;

    /** 코드값 동의어 (콤마 구분 문자열, 예: "미정산,대기") */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "synonyms", length = 500))
    private CodeValueSynonyms synonyms;

    /**
     * 코드값 생성 (불변식 강제).
     * <p>
     * schemaCatalogId 는 필수, code 는 공백일 수 없으며 대문자로 정규화한다.
     * id 는 application 레이어에서 생성해 주입한다.
     */
    public static CodeValue create(UUID codeValueId, UUID schemaCatalogId, String code,
                                   String label, String synonyms) {
        if (codeValueId == null) {
            throw new IllegalArgumentException("codeValueId 는 필수입니다.");
        }
        if (schemaCatalogId == null) {
            throw new IllegalArgumentException("schemaCatalogId 는 필수입니다.");
        }
        return CodeValue.builder()
                .codeValueId(new CodeValueId(codeValueId))
                .schemaCatalogId(new SchemaCatalogId(schemaCatalogId))
                .code(new Code(code))
                .label(label == null ? null : new Label(label))
                .synonyms(synonyms == null ? null : new CodeValueSynonyms(synonyms))
                .build();
    }

    /** 코드값 필드 수정 (JPA dirty checking) */
    public void update(String code, String label, String synonyms) {
        this.code = new Code(code);
        this.label = label == null ? null : new Label(label);
        this.synonyms = synonyms == null ? null : new CodeValueSynonyms(synonyms);
    }
}
