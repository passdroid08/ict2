package com.ict.project.simulator.calc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeResultDto {
    private int targetMonths;

    private long monthlySaving;
    private long projectedSavingTotal;
    private long projectedCashAtTarget;

    // "LOW/MID/HIGH"
    private String feasibilityByTime;
}
