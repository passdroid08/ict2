package com.ict.project.policy.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "POLICY_CONDITION")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PolicyCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_POLICY_CONDITION_GEN")
    @SequenceGenerator(name = "SEQ_POLICY_CONDITION_GEN", sequenceName = "SEQ_POLICY_CONDITION", allocationSize = 1)
    @Column(name = "CONDITION_ID", nullable = false)
    private Long conditionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "POLICY_ID", nullable = false)
    private PolicyEntity policy;

    @Column(name = "CONDITION_KEY", length = 50)
    private String conditionKey;

    @Column(name = "OPERATOR", length = 10)
    private String operator;

    @Column(name = "CONDITION_VALUE", length = 100)
    private String conditionValue;

    @Column(name = "VALUE_TYPE", length = 10, nullable = false)
    private String valueType;

    @Column(name = "CONDITION_VALUE_NUM", precision = 20, scale = 6)
    private BigDecimal conditionValueNum;

    @Column(name = "CONDITION_VALUE_DATE")
    private LocalDateTime conditionValueDate;

    @Column(name = "CONDITION_VALUE_BOOL", length = 1)
    private String conditionValueBool;
}
