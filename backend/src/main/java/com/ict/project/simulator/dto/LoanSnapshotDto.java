package com.ict.project.simulator.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoanSnapshotDto {
    private final Long userLoanId;
    private final Long loanId;
    private final BigDecimal approvedAmount;
    private final BigDecimal appliedRate;
    private final String status;
}
