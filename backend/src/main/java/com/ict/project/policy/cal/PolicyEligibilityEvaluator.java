package com.ict.project.policy.cal;

import org.springframework.stereotype.Component;

import com.ict.project.policy.dto.EligibilityResultDto;
import com.ict.project.policy.entity.PolicyEntity;
import com.ict.project.simulator.dto.ProfileSnapshotDto;

import lombok.NoArgsConstructor;


@Component
@NoArgsConstructor
public class PolicyEligibilityEvaluator {

    // 1) 단일 정책 자격 판정
    public EligibilityResultDto evaluate(ProfileSnapshotDto profile, PolicyEntity policy) {
    	
    	return  EligibilityResultDto.builder()
    			.applicable(false)
    			.conditions(null)
    			.reasons(null)
    			.build();
    }   
   
}