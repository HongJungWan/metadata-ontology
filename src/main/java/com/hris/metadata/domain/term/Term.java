package com.hris.metadata.domain.term;

import com.hris.metadata.global.common.BaseEntity;
import com.hris.metadata.shared.ddd.AggregateRoot;
import com.hris.metadata.shared.ddd.Subdomain;
import com.hris.metadata.shared.ddd.SubdomainType;
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
 * 표준 도메인 용어 (애그리거트 루트).
 * <p>
 * 같은 개념을 팀마다 다르게 부르는 표현을 하나의 정식 명칭(canonicalName)으로 모으는 SSOT 기준점이다.
 */
@AggregateRoot
@Subdomain(SubdomainType.CORE)
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

    /**
     * 표준 용어 생성 (불변식 강제).
     * <p>
     * canonicalName·domain 은 공백일 수 없으며, 상태는 ACTIVE 로 시작한다.
     * id 는 application 레이어에서 생성해 주입한다(도메인은 ID 생성기를 호출하지 않는다).
     */
    public static Term create(UUID termId, String canonicalName, String domain, String definition) {
        if (termId == null) {
            throw new IllegalArgumentException("termId 는 필수입니다.");
        }
        if (canonicalName == null || canonicalName.isBlank()) {
            throw new IllegalArgumentException("canonicalName 은 공백일 수 없습니다.");
        }
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException("domain 은 공백일 수 없습니다.");
        }
        return Term.builder()
                .termId(termId)
                .canonicalName(canonicalName)
                .domain(domain)
                .definition(definition)
                .status(TermStatus.ACTIVE)
                .build();
    }

    /** 용어 필드 수정 (JPA dirty checking). 상태 전이는 changeStatus 의 라이프사이클 규칙을 따른다. */
    public void update(String canonicalName, String domain, String definition, TermStatus status) {
        this.canonicalName = canonicalName;
        this.domain = domain;
        this.definition = definition;
        changeStatus(status);
    }

    /**
     * 상태 변경 (검토 후 활성화 등). 허용되지 않은 전이는 {@link IllegalStateException} 으로 거부한다.
     *
     * @see TermStatus#canTransitionTo(TermStatus)
     */
    public void changeStatus(TermStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status 는 필수입니다.");
        }
        if (this.status != null && !this.status.canTransitionTo(status)) {
            throw new IllegalStateException(
                    "허용되지 않은 상태 전이입니다: " + this.status + " → " + status);
        }
        this.status = status;
    }
}
