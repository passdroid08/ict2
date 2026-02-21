package com.ict.project.simulator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationCalculateResponseDto {
    
	private SummaryDto summaryDto;
	private FinanceSnapshotDto financeSnapshot;
	
    private List<PolicyImpactDto> policyList;
    private List<Long> appliedPolicyIds;
    
    private String explanation;

    private LocalDateTime calculatedAt; // 계산 완료 시각
}
