package com.hris.metadata.domain.pattern;

import com.hris.metadata.domain.pattern.vo.ColumnTarget;
import com.hris.metadata.domain.pattern.vo.TriggerKeywords;
import com.hris.metadata.domain.pattern.vo.ValueTemplate;
import com.hris.metadata.global.common.BaseEntity;
import com.hris.metadata.shared.ddd.AggregateRoot;
import com.hris.metadata.shared.ddd.Subdomain;
import com.hris.metadata.shared.ddd.SubdomainType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
 * SQL 패턴 규칙 (애그리거트 루트).
 * <p>
 * 트리거 키워드를 컬럼·연산자·값 템플릿에 매핑하는 규칙이다 (예: "미정산" → settlement_status EQ 'PENDING').
 * 실제 SQL 조립·실행은 소비자(P1) 몫이며, 본 서비스는 후보 매핑까지만 제공한다.
 */
@AggregateRoot
@Subdomain(SubdomainType.GENERIC)
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
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "trigger_keywords", nullable = false, length = 500))
    private TriggerKeywords triggerKeywords;

    /** 대상 컬럼 (예: settlement_status) */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "column_target", nullable = false, length = 200))
    private ColumnTarget columnTarget;

    /** 연산자 (EQ/IN/GTE/LTE/LIKE/BETWEEN) */
    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false, length = 20)
    private PatternOperator operator;

    /** 값 템플릿 (예: 'PENDING', ':timeRange' 등) */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "value_template", length = 500))
    private ValueTemplate valueTemplate;

    /** 우선순위 (낮을수록 우선) */
    @Column(name = "priority", nullable = false)
    private int priority;

    /**
     * SQL 패턴 생성 (불변식 강제).
     * <p>
     * triggerKeywords·columnTarget 은 공백일 수 없고 operator 는 필수다.
     * id 는 application 레이어에서 생성해 주입한다.
     */
    public static SqlPattern create(UUID sqlPatternId, String triggerKeywords, String columnTarget,
                                    PatternOperator operator, String valueTemplate, int priority) {
        if (sqlPatternId == null) {
            throw new IllegalArgumentException("sqlPatternId 는 필수입니다.");
        }
        if (operator == null) {
            throw new IllegalArgumentException("operator 는 필수입니다.");
        }
        return SqlPattern.builder()
                .sqlPatternId(sqlPatternId)
                .triggerKeywords(new TriggerKeywords(triggerKeywords))
                .columnTarget(new ColumnTarget(columnTarget))
                .operator(operator)
                .valueTemplate(valueTemplate == null ? null : new ValueTemplate(valueTemplate))
                .priority(priority)
                .build();
    }

    /** SQL 패턴 필드 수정 (JPA dirty checking) */
    public void update(String triggerKeywords, String columnTarget, PatternOperator operator,
                       String valueTemplate, int priority) {
        this.triggerKeywords = new TriggerKeywords(triggerKeywords);
        this.columnTarget = new ColumnTarget(columnTarget);
        this.operator = operator;
        this.valueTemplate = valueTemplate == null ? null : new ValueTemplate(valueTemplate);
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
        for (String trigger : triggerKeywords.value().split(",")) {
            if (trigger.trim().equals(normalized)) {
                return true;
            }
        }
        return false;
    }
}
