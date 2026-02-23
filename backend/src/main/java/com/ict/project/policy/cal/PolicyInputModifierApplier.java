package com.ict.project.policy.cal;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PolicyInputModifierApplier {
	
	
	
	public <T> T applyInputEffectsForPolicy(Long policyId, T financeInput, Long baselineLoanLimit) {
		
		
		return financeInput;		
	}
}