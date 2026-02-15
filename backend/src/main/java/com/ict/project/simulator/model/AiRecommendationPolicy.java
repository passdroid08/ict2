package com.ict.project.simulator.model;

import com.ict.project.policy.model.PolicyEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "AI_RECOMMENDATION_POLICY")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AiRecommendationPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AI_RECOMMENDATION_POLICY_GEN")
    @SequenceGenerator(name = "SEQ_AI_RECOMMENDATION_POLICY_GEN", sequenceName = "SEQ_AI_RECOMMENDATION_POLICY", allocationSize = 1)
    @Column(name = "REC_POLICY_ID", nullable = false)
    private Long recPolicyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "REC_ID", nullable = false)
    private AiRecommendation recommendation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "POLICY_ID", nullable = false)
    private PolicyEntity policy;

    @Column(name = "PRIORITY", nullable = false)
    private Integer priority;

    @Column(name = "REASON", length = 2000)
    private String reason;

    @Column(name = "IMPACT_ESTIMATE", precision = 15, scale = 0)
    private BigDecimal impactEstimate;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;
}
