package com.ict.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "LOAN_PRODUCT")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LoanProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_LOAN_PRODUCT_GEN")
    @SequenceGenerator(name = "SEQ_LOAN_PRODUCT_GEN", sequenceName = "SEQ_LOAN_PRODUCT", allocationSize = 1)
    @Column(name = "LOAN_ID", nullable = false)
    private Long loanId;

    @Column(name = "BANK_NAME", length = 100, nullable = false)
    private String bankName;

    @Column(name = "LOAN_NAME", length = 150)
    private String loanName;

    @Column(name = "LOAN_TYPE", length = 30)
    private String loanType;

    @Column(name = "INTEREST_TYPE", length = 20)
    private String interestType;

    @Column(name = "BASE_RATE", precision = 5, scale = 3)
    private BigDecimal baseRate;

    @Column(name = "MAX_LTV", precision = 5, scale = 2)
    private BigDecimal maxLtv;

    @Column(name = "MAX_DTI", precision = 5, scale = 2)
    private BigDecimal maxDti;

    @Column(name = "TERM_MONTHS")
    private Integer termMonths;
}
