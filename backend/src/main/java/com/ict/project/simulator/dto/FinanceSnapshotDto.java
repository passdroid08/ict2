package com.ict.project.simulator.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FinanceSnapshotDto {
    private Long userId;

    private BigDecimal annualIncome;
    private BigDecimal assetAmount;
    private BigDecimal debtAmount;

    private BigDecimal cashAvailable;
    private BigDecimal emergencyFund;
    private BigDecimal monthlyHousingBudget;

    private String loanPreference;

    private Integer targetMonths;
    private BigDecimal targetPropertyPrice;
}