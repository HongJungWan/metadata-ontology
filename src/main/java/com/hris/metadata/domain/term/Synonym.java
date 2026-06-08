package com.hris.metadata.domain.term;

import com.hris.metadata.global.common.BaseEntity;
import com.hris.metadata.shared.ddd.AggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * 동의어.
 * <p>
 * 자연어에서 등장하는 다양한 표현(약어/한영/오타/구어)을 표준 용어로 잇는다.
 * 표준 용어는 식별자(termId)로만 참조한다 (애그리거트 간 ID 참조).
 */
@AggregateRoot
@Entity
@Table(name = "synonym", schema = "meta",
        indexes = @Index(name = "idx_synonym_surface", columnList = "surface"))
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

    /**
     * 동의어 생성 (불변식 강제).
     * <p>
     * termId 는 필수, surface 는 공백일 수 없다.
     * id 는 application 레이어에서 생성해 주입한다.
     */
    public static Synonym create(UUID synonymId, UUID termId, String surface, SynonymType type) {
        if (synonymId == null) {
            throw new IllegalArgumentException("synonymId 는 필수입니다.");
        }
        if (termId == null) {
            throw new IllegalArgumentException("termId 는 필수입니다.");
        }
        if (surface == null || surface.isBlank()) {
            throw new IllegalArgumentException("surface 는 공백일 수 없습니다.");
        }
        return Synonym.builder()
                .synonymId(synonymId)
                .termId(termId)
                .surface(surface)
                .type(type)
                .build();
    }

    /** 동의어 필드 수정 (JPA dirty checking) */
    public void update(String surface, SynonymType type) {
        this.surface = surface;
        this.type = type;
    }
}
