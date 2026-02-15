package com.ict.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "USER_LOAN")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_USER_LOAN_GEN")
    @SequenceGenerator(name = "SEQ_USER_LOAN_GEN", sequenceName = "SEQ_USER_LOAN", allocationSize = 1)
    @Column(name = "USER_LOAN_ID", nullable = false)
    private Long userLoanId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private Users users;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "LOAN_ID", nullable = false)
    private LoanProduct loanProduct;

    @Column(name = "APPROVED_AMOUNT", precision = 15, scale = 0)
    private BigDecimal approvedAmount;

    @Column(name = "APPLIED_RATE", precision = 5, scale = 3)
    private BigDecimal appliedRate;

    @Column(name = "APPROVED_DATE")
    private LocalDateTime approvedDate;

    @Column(name = "STATUS", length = 20)
    private String status;
}
