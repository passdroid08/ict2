package com.ict.project.simulator.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyImpactViewDto {
	
	private Long policyId;
	private String name;

	private Long impactAmount;    
	private Double impactPercent;  
	private Long monthlyImpact;    

	private List<String> reasons;
	private String reasonSummary;
	private List<String> conditions;
	private String caution;
	
	public Number getLoanDelta() {
	    return impactAmount;
	}

}
