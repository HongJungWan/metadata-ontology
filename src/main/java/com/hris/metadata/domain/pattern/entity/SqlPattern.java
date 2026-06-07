package com.hris.metadata.domain.pattern.entity;

import com.hris.metadata.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
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
 * SQL 패턴 규칙.
 * <p>
 * 트리거 키워드를 컬럼·연산자·값 템플릿에 매핑하는 규칙이다 (예: "미정산" → settlement_status EQ 'PENDING').
 * 실제 SQL 조립·실행은 소비자(P1) 몫이며, 본 서비스는 후보 매핑까지만 제공한다.
 */
@Entity
@Table(name = "sql_pattern", schema = "meta")
@Where(clause = "deleted_at IS NULL")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class SqlPattern extends BaseEntity {

    /** SQL 패턴 ID (PK) */
    @Id
    @Column(name = "sql_pattern_id", nullable = false, columnDefinition = "uuid")
    private UUID sqlPatternId;

    /** 트리거 키워드 (콤마 구분, 예: "미정산,세틀미완료") */
    @Column(name = "trigger_keywords", nullable = false, length = 500)
    private String triggerKeywords;

    /** 대상 컬럼 (예: settlement_status) */
    @Column(name = "column_target", nullable = false, length = 200)
    private String columnTarget;

    /** 연산자 (EQ/IN/GTE/LTE/LIKE/BETWEEN) */
    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false, length = 20)
    private PatternOperator operator;

    /** 값 템플릿 (예: 'PENDING', ':timeRange' 등) */
    @Column(name = "value_template", length = 500)
    private String valueTemplate;

    /** 우선순위 (낮을수록 우선) */
    @Column(name = "priority", nullable = false)
    private int priority;

    /** SQL 패턴 필드 수정 (JPA dirty checking) */
    public void update(String triggerKeywords, String columnTarget, PatternOperator operator,
                       String valueTemplate, int priority) {
        this.triggerKeywords = triggerKeywords;
        this.columnTarget = columnTarget;
        this.operator = operator;
        this.valueTemplate = valueTemplate;
        this.priority = priority;
    }

    /**
     * 주어진 키워드가 이 패턴의 트리거 키워드 중 하나에 일치하는지 확인한다.
     */
    public boolean matchesKeyword(String keyword) {
        if (keyword == null || triggerKeywords == null) {
            return false;
        }
        String normalized = keyword.trim();
        for (String trigger : triggerKeywords.split(",")) {
            if (trigger.trim().equals(normalized)) {
                return true;
            }
        }
        return false;
    }
}
