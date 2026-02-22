package com.ict.project.simulator.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileSnapshotDto {
    private final Long userId;
    private final String name;
    private final Integer age;
    private final String gender;
    private final String maritalStatus;

    private final BigDecimal annualIncome;
    private final BigDecimal assetAmount;
    private final BigDecimal debtAmount;

    private final BigDecimal cashAvailable;
    private final BigDecimal emergencyFund;
    private final BigDecimal monthlyHousingBudget;
    private final String loanPreference;
    private final Integer targetMonths;
    private final BigDecimal targetPropertyPrice;

    private final List<LoanSnapshotDto> loans;
    private final List<PreferenceSnapshotDto> preferences;
    private final List<InterestRegionSnapshotDto> interestRegions;
}
