package com.ict.project.simulator.model;

import com.ict.project.model.UserLoan;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "COST_LOAN_APPLIED")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CostLoanApplied {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_COST_LOAN_APPLIED_GEN")
    @SequenceGenerator(name = "SEQ_COST_LOAN_APPLIED_GEN", sequenceName = "SEQ_COST_LOAN_APPLIED", allocationSize = 1)
    @Column(name = "COST_LOAN_ID", nullable = false)
    private Long costLoanId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COST_ID", nullable = false)
    private CostResult costResult;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_LOAN_ID", nullable = false)
    private UserLoan userLoan;

    @Column(name = "USED_AMOUNT", precision = 15, scale = 0)
    private BigDecimal usedAmount;

    @Column(name = "RATE_AT_TIME", precision = 5, scale = 3)
    private BigDecimal rateAtTime;

    @Column(name = "NOTE", length = 200)
    private String note;
}
