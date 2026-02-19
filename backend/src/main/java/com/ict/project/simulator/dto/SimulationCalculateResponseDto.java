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
public class SimulationCalculateResponseDto {
    
	private SummaryDto summaryDto;

    private List<PolicyImpactDto> policyList;
    private List<Long> appliedPolicyIds;
    
    private String explanation;

    private LocalDateTime calculatedAt; // 계산 완료 시각
}
