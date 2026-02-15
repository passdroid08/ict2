package com.ict.project.policy.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "POLICY_EFFECT")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PolicyEffect {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_POLICY_EFFECT_GEN")
    @SequenceGenerator(name = "SEQ_POLICY_EFFECT_GEN", sequenceName = "SEQ_POLICY_EFFECT", allocationSize = 1)
    @Column(name = "EFFECT_ID", nullable = false)
    private Long effectId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "POLICY_ID", nullable = false)
    private PolicyEntity policy;

    @Column(name = "EFFECT_KEY", length = 50)
    private String effectKey;

    @Column(name = "OPERATOR", length = 10)
    private String operator;

    @Column(name = "EFFECT_VALUE", length = 100)
    private String effectValue;

    @Column(name = "UNIT", length = 10)
    private String unit;

    @Column(name = "PRIORITY")
    private Integer priority;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    @Column(name = "VALUE_TYPE", length = 10, nullable = false)
    private String valueType;

    @Column(name = "EFFECT_VALUE_NUM", precision = 20, scale = 6)
    private BigDecimal effectValueNum;

    @Column(name = "EFFECT_VALUE_DATE")
    private LocalDateTime effectValueDate;

    @Column(name = "EFFECT_VALUE_BOOL", length = 1)
    private String effectValueBool;
}
