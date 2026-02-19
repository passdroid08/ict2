package com.ict.project.simulator.model;

import com.ict.project.model.Property;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "COST_RESULT")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CostResult {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_COST_RESULT_GEN")
    @SequenceGenerator(name = "SEQ_COST_RESULT_GEN", sequenceName = "SEQ_COST_RESULT", allocationSize = 1)
    @Column(name = "COST_ID", nullable = false)
    private Long costId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROPERTY_ID", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SNAPSHOT_ID", nullable = false)
    private FinanceSnapshot financeSnapshot;

    @Column(name = "TAX_AMOUNT", precision = 15, scale = 0)
    private BigDecimal taxAmount;

    @Column(name = "LOAN_AMOUNT", precision = 15, scale = 0)
    private BigDecimal loanAmount;

    @Column(name = "TOTAL_COST", precision = 15, scale = 0)
    private BigDecimal totalCost;

    @Column(name = "CALCULATED_AT", nullable = false)
    private LocalDateTime calculatedAt;

    @PrePersist
    public void prePersist() {
        if (calculatedAt == null) calculatedAt = LocalDateTime.now();
    }

}
