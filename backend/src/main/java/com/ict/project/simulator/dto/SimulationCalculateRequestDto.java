package com.ict.project.simulator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationCalculateRequestDto {

    private Long userId;

    private Integer cashAvailable;
    private Integer emergencyFund;
    private Integer monthlyHousingBudget;

    private String loanPreference;
    private Integer targetMonths;
    private Integer targetPropertyPrice;

    // 사용자가 최종 선택한 정책들
    private List<Long> selectedPolicyIds;

    private LocalDateTime requestedAt;
}
