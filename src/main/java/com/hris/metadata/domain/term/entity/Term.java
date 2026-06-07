package com.hris.metadata.domain.term.entity;

import com.hris.metadata.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Where;

import java.util.UUID;

/**
 * 표준 도메인 용어.
 * <p>
 * 같은 개념을 팀마다 다르게 부르는 표현을 하나의 정식 명칭(canonicalName)으로 모으는 SSOT 기준점이다.
 */
@Entity
@Table(name = "term", schema = "meta",
        uniqueConstraints = @UniqueConstraint(name = "uk_term_canonical_name", columnNames = "canonical_name"))
@Where(clause = "deleted_at IS NULL")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class Term extends BaseEntity {

    /** 표준 용어 ID (PK) */
    @Id
    @Column(name = "term_id", nullable = false, columnDefinition = "uuid")
    private UUID termId;

    /** 정식 명칭 (예: 정산금액) */
    @Column(name = "canonical_name", nullable = false, length = 200)
    private String canonicalName;

    /** 도메인 (예: settlement) */
    @Column(name = "domain", length = 100)
    private String domain;

    /** 용어 정의 */
    @Column(name = "definition", length = 1000)
    private String definition;

    /** 거버넌스 상태 (ACTIVE/DRAFT/DEPRECATED) */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TermStatus status;

    /** 용어 필드 수정 (JPA dirty checking) */
    public void update(String canonicalName, String domain, String definition, TermStatus status) {
        this.canonicalName = canonicalName;
        this.domain = domain;
        this.definition = definition;
        this.status = status;
    }

    /** 상태 변경 (검토 후 활성화 등) */
    public void changeStatus(TermStatus status) {
        this.status = status;
    }
}
