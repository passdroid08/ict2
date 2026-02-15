package com.ict.project.simulator.model;

import com.ict.project.policy.model.PolicyEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "COST_POLICY_APPLIED")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CostPolicyApplied {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_COST_POLICY_APPLIED_GEN")
    @SequenceGenerator(name = "SEQ_COST_POLICY_APPLIED_GEN", sequenceName = "SEQ_COST_POLICY_APPLIED", allocationSize = 1)
    @Column(name = "COST_POLICY_ID", nullable = false)
    private Long costPolicyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COST_ID", nullable = false)
    private CostResult costResult;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "POLICY_ID", nullable = false)
    private PolicyEntity policy;

    @Column(name = "IMPACT_AMOUNT", precision = 15, scale = 0)
    private BigDecimal impactAmount;

    @Column(name = "NOTE", length = 200)
    private String note;
}
