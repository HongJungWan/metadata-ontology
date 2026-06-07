package com.hris.metadata.domain.term.entity;

import com.hris.metadata.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
 * 동의어.
 * <p>
 * 자연어에서 등장하는 다양한 표현(약어/한영/오타/구어)을 표준 용어로 잇는다.
 */
@Entity
@Table(name = "synonym", schema = "meta",
        indexes = @jakarta.persistence.Index(name = "idx_synonym_surface", columnList = "surface"))
@Where(clause = "deleted_at IS NULL")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class Synonym extends BaseEntity {

    /** 동의어 ID (PK) */
    @Id
    @Column(name = "synonym_id", nullable = false, columnDefinition = "uuid")
    private UUID synonymId;

    /** 표준 용어 연관관계 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id", insertable = false, updatable = false)
    @ToString.Exclude
    private Term term;

    /** 표준 용어 ID (FK) */
    @Column(name = "term_id", nullable = false, columnDefinition = "uuid")
    private UUID termId;

    /** 표면형 표현 (예: 세틀) */
    @Column(name = "surface", nullable = false, length = 200)
    private String surface;

    /** 동의어 유형 (약어/한영/오타/구어) */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private SynonymType type;

    /** 동의어 필드 수정 (JPA dirty checking) */
    public void update(String surface, SynonymType type) {
        this.surface = surface;
        this.type = type;
    }
}
