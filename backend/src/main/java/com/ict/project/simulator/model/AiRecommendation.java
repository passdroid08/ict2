package com.ict.project.simulator.model;

import com.ict.project.model.Property;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "AI_RECOMMENDATION")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AiRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AI_RECOMMENDATION_GEN")
    @SequenceGenerator(name = "SEQ_AI_RECOMMENDATION_GEN", sequenceName = "SEQ_AI_RECOMMENDATION", allocationSize = 1)
    @Column(name = "REC_ID", nullable = false)
    private Long recId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROPERTY_ID", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COST_ID")
    private CostResult costResult;

    @Column(name = "REASON", length = 2000)
    private String reason;

    @Column(name = "SCORE", precision = 5, scale = 4)
    private BigDecimal score;

    @Column(name = "RECOMMENDED_AT", nullable = false)
    private LocalDateTime recommendedAt;

    @PrePersist
    public void prePersist() {
        if (recommendedAt == null) recommendedAt = LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INFER_ID")
    private AiInferenceLog inferenceLog;

    @Column(name = "REC_TYPE", length = 30, nullable = false)
    private String recType;

    @Column(name = "REC_STATUS", length = 20, nullable = false)
    private String recStatus;
}
