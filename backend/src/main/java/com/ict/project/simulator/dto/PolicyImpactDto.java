package com.ict.project.simulator.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyImpactDto {
	
	private Long policyId;
	private String name;

	private Long impactAmount;     // 기존 loanDelta 대체 가능
	private Double impactPercent;  // 계산 필요
	private Long monthlyImpact;    // 기존 monthlyDelta

	private List<String> reasons;
	private String reasonSummary;
	private List<String> conditions;
	private String caution;

}
